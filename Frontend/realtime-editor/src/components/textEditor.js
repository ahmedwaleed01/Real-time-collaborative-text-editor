import React, { useEffect, useState, useRef } from "react";
import { useEditor, EditorContent } from "@tiptap/react";
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
import TextAlign from "@tiptap/extension-text-align";

import { useQuery } from "react-query";
import { useParams } from "react-router-dom";
import axios from "axios";
class CharacterData {
  constructor(character, id, bold = false) {
    this.character = character;
    this.id = id;
    this.bold = bold;
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

  isBold() {
    return this.bold;
  }

  setBold(bold) {
    this.bold = bold;
  }
}

let charactersData = [];
let operationId = 0;
function getDiff(oldContent, newContent) {
  let position = 0;
  // Find the first position where the old and new content differ
  console.log("old content", oldContent);
  console.log("new content", newContent);
  if (oldContent.length == 0) {
    let first = new CharacterData(newContent[0], 1);
    charactersData.push(first);
  }
  while (
    position < oldContent.length &&
    oldContent[position] === newContent[position]
  ) {
    position++;
  }

  if (newContent.length > oldContent.length) {
    operationId++;
    return {
      type: "insert",
      indexStart: position,
      indexEnd: position + 1,
      charValue: newContent[position],
      attributes: {},
      id: operationId,
    };
  } else if (newContent.length < oldContent.length) {
    operationId++;
    return {
      type: "delete",
      indexStart: position,
      indexEnd: position + 1,
      charValue: oldContent[position],
      attributes: {},
      id: operationId,
    };
  }

  return null;
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
  TextAlign,
];

const fetchContent = async (documentId) => {
  const token = localStorage.getItem("token");
  const res = await axios.get(
    process.env.REACT_APP_API_URL + `/dc/view/${documentId}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return res.data;
};

const TextEditor = () => {
  const { documentId } = useParams();
  const [content, setContent] = useState();
  const {
    data: Document,
    isLoading,
    isError,
  } = useQuery([documentId], () => fetchContent(documentId));
  const [socket, setSocket] = useState(null);
  const [messages, setMessages] = useState("");
  const [cursor, setCursor] = useState(0);
  const [data, setData] = useState([]);
  const [DataTobeSaved, setDataToBeSaved] = useState();
  const [oldContent, setOldContent] = useState("");
  const [NewContent, setNewContent] = useState("");
  const [DocumentContent, setDocumentContent] = useState("");
  const extractContent = (data) => {
    let con = "";
    let contentBegin = "";

    console.log("Data received henaa:", data);
    if (Array.isArray(data)) {
      data.forEach((item) => {
        let notBOf = item.char != "bof" && item.char != "eof";
        if (notBOf && item.isItalic && item.bold && !item.flagDelete) {
          con += `<b><i>${item.char}</i></b>`;
        } else if (notBOf && item.bold && !item.flagDelete) {
          con += `<b>${item.char}</b>`;
        } else if (notBOf && item.isItalic && !item.flagDelete) {
          con += `<i>${item.char}</i>`;
        } else if (notBOf && item.underLine && !item.flagDelete) {
          con += `<u>${item.char}</u>`;
        } else if (notBOf && !item.flagDelete) {
          con += item.char;
        }
        if (notBOf && !item.flagDelete) {
          contentBegin += item.char;
        }
      });
    } else {
      console.error("Expected an array, but received:", typeof data);
    }
    setOldContent(contentBegin);
    console.log("Content baaaaaaaaaaa", con);
    return con;
  };

  const saveContent = async (data) => {
    try {
      const token = localStorage.getItem("token");
      console.log("data to be saved", data);
      const res = await axios.post(
        process.env.REACT_APP_API_URL + `/dc/save/${documentId}`,
        {
          content: data,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      console.log("Document Saved", res.data);
    } catch (error) {
      console.error("Error:", error);
    }
  };
  const [permission, setpermission] = useState('');
  const [editPermission, setEditPermission] = useState(false);
  useEffect(() => {
    const fetchData = async () => {
      const token = localStorage.getItem("token");
      const res = await axios.get(
        process.env.REACT_APP_API_URL + `/dc/getUserAccessLevel/${documentId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      console.log("res.data", res.data);
      setpermission(res.data);
      if (res.data === "EDITOR" || res.data === "OWNER") {
        setEditPermission(true);
      }

      // console.log("permission", permission);

    };

    fetchData();
  }, [documentId]);

  useEffect(() => {
    const socket = new WebSocket(`ws://localhost:8000/api/topic`);
    socket.onopen = () => {
      console.log("WebSocket connected");
      const data = { documentId: documentId, content: "" };
      const jsonData = JSON.stringify(data);
      socket.send(jsonData);
    };

    socket.onmessage = (event) => {
      // response from server
      console.log("Received data", event.data);
      const eventData = event.data;
      setDataToBeSaved(eventData);
      setData(eventData);
    };
    socket.onerror = (error) => {
      console.error("WebSocket error:", error);
    };

    setSocket(socket);

    return () => {
      socket.close();
    };
  }, [documentId]);
  const [idCounter, setIdCounter] = useState(0);
  function generateUniqueId() {
    setIdCounter((prevCounter) => prevCounter + 1);
    return `${idCounter}`;
  }

  const sendContentToServer = (
    operationType,
    indexStart,
    indexEnd,
    character,
    attributes,
    id
  ) => {
    if (socket) {
      const isBold = editor.isActive("bold");
      const isItalic = editor.isActive("italic");
      const isUnderLine = editor.isActive("underline");
      const data = {
        documentId: documentId,
        operation: {
          operationType: operationType,
          indexStart: indexStart,
          indexEnd: indexEnd,
          charValue: character,
          attributes: {
            bold: isBold,
            italic: isItalic,
            underLine: isUnderLine,
          },
          id: id,
        },
      };

      const jsonData = JSON.stringify(data);
      socket.send(jsonData);
    }
  };

  useEffect(() => {
    console.log("Sssssssssssssssssss");
    if (editor) {
      console.log("Sssssssssssssss", data);
      editor.commands.setContent(extractContent(JSON.parse(data)));
      editor?.commands.setTextSelection(cursor);
    }
  }, [data]);
  // const NOTEditable=permission!=="VIEWER";
  const editor = useEditor({
    extensions: extensions,
    content: DocumentContent,
    // editable: editPermission,
    onUpdate: ({ editor }) => {
      const newContent = editor.getText();
      console.log("contenttttttttttt", content, "Sssss", newContent);
      const diff = getDiff(oldContent, newContent);
      const { from, to } = editor.state.selection;
      setCursor(from);
      console.log("cursor position is ", from, " ", to);
      setOldContent(newContent);
      if (diff) {
        console.log("Diff= ", diff);
        if (diff.type === "insert") {
          sendContentToServer(
            "insertCharacter",
            diff.indexStart,
            diff.indexEnd,
            diff.charValue,
            diff.attributes
            // diff.id
          );
        } else if (diff.type === "delete") {
          sendContentToServer(
            "deleteCharacter",
            diff.indexStart,
            diff.indexEnd,
            diff.charValue,
            diff.attributes
            // diff.id
          );
        }
      }
      // setContent(newContent);
    },
  });

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>Error fetching content</div>;
  }

  return (
    <div className="container border mt-4 vh-100 w-50">
      <MenuBar editor={editor} />
      {/* <EditorContent editor={editor} className="border-none" /> */}
      {
        console.log("permission in HTML", permission)
      }
      {
        console.log("isVIEWER", permission === "VIEWER")
      }
      {
        console.log("editPermission in HTML", editPermission)
      }
      {
        // console.log("editor.options.editable", editor.options.editable)
        console.log(
          editor && editor.options && editor.options.editable !== null 
            ? "editor.options.editable: " + editor.options.editable 
            : ''
        )
    }
      {/* <EditorContent editor={editor} className="border-none" /> */}
      <EditorContent editor={editor} readOnly={!editPermission} className="border-none" />
      {/* {permission ? <EditorContent editor={editor} className="border-none" /> : <div>Determining access permissions, please wait!...</div>} */}
    </div>
  );
};

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
    </div>
  );
};
