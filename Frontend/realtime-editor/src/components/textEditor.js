import React, { useEffect } from "react";
import { useEditor, EditorContent, EditorProvider } from "@tiptap/react";
import { Button } from "react-bootstrap";
import { BiHeading, BiBold, BiItalic, BiStrikethrough } from "react-icons/bi";
import { AiOutlineOrderedList } from "react-icons/ai";

import StarterKit from "@tiptap/starter-kit";
import Heading from "@tiptap/extension-heading";
import Superscript from "@tiptap/extension-superscript";
import Link from "@tiptap/extension-link";
import Blockquote from "@tiptap/extension-blockquote";
import Code from "@tiptap/extension-code";
import CodeBlock from "@tiptap/extension-code-block";
import BulletList from "@tiptap/extension-bullet-list";
import OrderedList from "@tiptap/extension-ordered-list";
import ListItem from "@tiptap/extension-list-item";
import TextAlign from "@tiptap/extension-text-align";

import { useQuery } from "react-query";
import { useParams } from "react-router-dom";
import axios from "axios";
// import { Node } from 'tiptap';
class CharacterData {
  // constructor()
  // {}
  constructor(character, id) {
    this.character = character;
    this.id = id;
  }

  getCharacter() {
    return this.character;
  }

  setCharacter(character) {
    this.character = character;
  }

  getId() {
    return this.id;
  }

  setId(id) {
    this.id = id;
  }
}
let idCounter = 0;

function generateUniqueId() {
  idCounter += 1;
  return idCounter;
}
function getDiff(oldContent, newContent) {
  let index = 0;

  // Convert Maps to arrays of entries for comparison
  const oldContentArray = Array.from(oldContent.entries());
  const newContentArray = Array.from(newContent.entries());

  if (oldContentArray.length > 0 && newContentArray.length > 0) {
    while (index < newContentArray.length) {
    while (oldContentArray[index][0] === newContentArray[index][0]) {
      index++;
    }
  }
  }

  if (oldContentArray.length !== newContentArray.length) {
    if (oldContentArray.length < newContentArray.length) {
      // A character was inserted
      let fractionalIndex;
      if (index === 0) {
        fractionalIndex = generateFractionalIndex(0, newContentArray[index][0]);
      } else if (index === newContentArray.length - 1) {
        fractionalIndex = generateFractionalIndex(
          newContentArray[index - 1][0],
          idCounter + 1
        );
      } else {
        if (index > 0) {
          fractionalIndex = generateFractionalIndex(
            newContentArray[index - 1][0],
            newContentArray[index][0]
          );
        } else {
          fractionalIndex = 0;
        }
      }
      return {
        type: "insert",
        index: fractionalIndex,
        charValue: newContentArray[index][1].getCharacter(),
        attributes: {}, 
        id: newContentArray[index][1].getId(),
      };
    } else {
      // A character was deleted
      return {
        type: "delete",
        index: oldContentArray[index][0],
        charValue: oldContentArray[index][1].getCharacter(),
        attributes: {}, 
        id: oldContentArray[index][1].getId(),
      };
    }
  }

  // If the lengths are the same and we got this far, there are no differences
  return null;
}

function generateFractionalIndex(index1, index2) {
  return (index1 + index2) / 2;
}
const extensions = [
  StarterKit,
  Heading,
  Superscript,
  Link,
  Blockquote,
  Code,
  CodeBlock.configure({
    HTMLAttributes: {
      class: "rounded-sm bg-neutral-200 p-2",
    },
  }),
  BulletList,
  OrderedList,
  ListItem,
  TextAlign,
];

const TextEditor = () => {
  // const initialContent = "";
  // const initialContentArray = Array.from(initialContent).map(
  //   (char, index) => new CharacterData(char, generateUniqueId())
  // );
  // Initialize content as a Map instead of an array
  const [content, setContent] = useState(new Map());
  const { documentId } = useParams();
  const {
    data: Document,
    isLoading,
    isError,
  } = useQuery([documentId], () => fetchContent(documentId));
  const [socket, setSocket] = useState(null);
  const [messages, setMessages] = useState("");

  // useEffect(() => {
  //   if (Document) {
  //     setContent(Document.title);
  //   }
  // }, [Document]);

  useEffect(() => {
    const socket = new WebSocket(`ws://localhost:8000/api/topic`);
    // socket.onerror = function(event) {
    //   console.error("WebSocket error observed:", event);
    // };
    socket.onopen = () => {
      console.log("WebSocket connected");
      const data = { documentId: documentId, content: "" };
      const jsonData = JSON.stringify(data);
      socket.send(jsonData);
    };

    socket.onmessage = (event) => {
      // response from server
      try{
      const eventData = JSON.parse(event.data);
      console.log("Received data", eventData);
      const updatedSequence = eventData.sequence;
      console.log("Updated Sequence", updatedSequence);
      const updatedChar = eventData.updatedChar;

      // Update the content state with the new sequence
      const newContentMap = new Map(
        updatedSequence.map((charData) => {
          return [
            charData.id,
            new CharacterData(charData.charValue, charData.id),
          ];
        })
      );
      setContent(newContentMap);
    } catch (error) {
      console.error("Error in on message", error);
    }
    };
    socket.onerror = (error) => {
      console.error("WebSocket error:", error);
    };

    setSocket(socket);

    return () => {
      socket.close();
    };
  }, [documentId]);
  // const [idCounter, setIdCounter] = useState(0);
  // function generateUniqueId() {
  //   setIdCounter((prevCounter) => prevCounter + 1);
  //   return `${idCounter}`;
  // }

  // const sendContentToServer = (content) => {
  //   if (socket) {
  //     const data = {
  //       documentId: documentId,
  //       content: content,
  //     };

  //     const jsonData = JSON.stringify(data);
  //     socket.send(jsonData);
  //   }
  // };
  const sendContentToServer = (
    operationType,
    index,
    character,
    attributes,
    id
  ) => {
    if (socket) {
      const data = {
        documentId: documentId,
        operation: { 
          operationType: operationType,
          index: parseFloat(index),
          charValue: character,
          attributes: {},
          id: id.toString(),
        },
      }; 

      const jsonData = JSON.stringify(data);
      socket.send(jsonData);
    }
  };

  const editor = useEditor({
    extensions: extensions,
    content: Array.from(content.values())
      .map((characterData) => characterData.getCharacter())
      .join(""),
    onUpdate: ({ editor }) => {
      const newContentString = editor.getText();
      // Update the onUpdate callback in useEditor to work with a Map
      const newContentMap = new Map(
        Array.from(newContentString).map((char, index) => {
          // If the character at this index already exists in the old content, use the same ID
          if (content.has(index)) {
            return [index, new CharacterData(char, content.get(index).getId())];
          }
          // Otherwise, generate a new ID for the new character
          else {
            return [index, new CharacterData(char, generateUniqueId())];
          }
        })
      );
      const diff = getDiff(content, newContentMap);
      if (diff) {
        console.log("Diff= ", diff);
        if (diff.type === "insert") {
          sendContentToServer(
            "insertCharacter",
            diff.index,
            diff.charValue,
            diff.attributes,
            diff.id
          );
        } else if (diff.type === "delete") {
          sendContentToServer(
            "deleteCharacter",
            diff.index,
            diff.charValue,
            diff.attributes,
            diff.id
          );
        }
      }
      setContent(newContentMap);
    },
  });
  useEffect(() => {
    console.log("Sssssssssssssssssss");
    if (editor) {
      console.log("Sssssssssssssss");
      try {
        let contentString = Array.from(content.values())
          .map((characterData) => characterData.getCharacter())
          .join("");
  
        // Now, you can set the content of the editor
        editor.commands.setContent(contentString);
      } catch (error) {
        console.error("Error updating editor content:", error);
      }
    }
  }, [content])

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>Error fetching content</div>;
  }

  return (
    <div className="container border mt-4 vh-100 w-50">
      <MenuBar editor={editor} />
      <EditorContent editor={editor} className="border-none" />
    </div>
  );
};
let ws = new WebSocket('ws://localhost:8000/ws'); 
function sendContentToServer(content) {
  const message = {
    type: 'edit',
    content: content,
    // userId: userId, //// TODO: Add the userId here
  };
  ws.send(JSON.stringify(message));
}

function RecieveFromServer(callback) {
  // Set up a listener for updates from the server
  ws.onopen = () => {
  ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    if (message.type === 'edit') {
      callback(message.content);
    }
  };
  };
  // Return a function that removes the listener
  return () => {
    ws.onmessage = null;
  };
}
export default TextEditor;

const MenuBar = ({ editor }) => {
  if (!editor) {
    return null;
  }
  return (
    <div className="d-flex justify-content-around align-items-center container mb-4 mt-4">
      <Button
        variant="outline-dark"
        size="sm"
        onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
        className={
          editor.isActive("heading", { level: 1 }) ? "btn-primary" : ""
        }
      >
        <BiHeading size={20} />
      </Button>
      <Button
        variant="outline-dark"
        size="sm"
        onClick={() => editor.chain().focus().toggleBold().run()}
        className={editor.isActive("bold") ? "btn-primary" : ""}
      >
        <BiBold size={20} />
      </Button>
      <Button
        variant="outline-dark"
        size="sm"
        onClick={() => editor.chain().focus().toggleItalic().run()}
        className={editor.isActive("italic") ? "btn-primary" : ""}
      >
        <BiItalic size={20} />
      </Button>
      <Button
        variant="outline-dark"
        size="sm"
        onClick={() => editor.chain().focus().toggleStrike().run()}
        className={editor.isActive("strike") ? "btn-primary" : ""}
      >
        <BiStrikethrough size={20} />
      </Button>
      <Button
        variant="outline-dark"
        size="sm"
        onClick={() => editor.chain().focus().toggleOrderedList().run()}
        className={editor.isActive("orderedList") ? "btn-primary" : ""}
      >
        <AiOutlineOrderedList size={20} />
      </Button>
      {/* Add more buttons for other functionality as needed */}
    </div>
  );
};
