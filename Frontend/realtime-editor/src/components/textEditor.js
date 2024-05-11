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
function getDiff(oldContent, newContent) {
  let position = 0;

  while (
    position < oldContent.length &&
    oldContent[position] === newContent[position]
  ) {
    position++;
  }
  let beforeId = oldContent.length === 0 ? "-1" : (position - 1).toString();
  let afterId = newContent.length === 0 ? newContent[position]?.id : "10000";

  if (newContent.length > oldContent.length) {
    return {
      type: "insert",
      position: position,
      character: newContent[position],
      beforeId: beforeId,
      afterId: afterId,
    };
  } else if (newContent.length < oldContent.length) {
    return {
      type: "delete",
      position: position,
      characterId: oldContent[position]?.id,
      beforeId: beforeId,
      afterId: afterId,
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
  const [content, setContent] = useState("");
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
      console.log("Received data", event.data);
      const eventData = JSON.parse(event.data);
      const content = eventData.content;
      const operation = eventData.operation;

      if (operation.type === "insertCharacter") {
        const beforeIndex = content.findIndex(
          (crdt) => crdt.id === operation.beforeId
        );
        const afterIndex = content.findIndex(
          (crdt) => crdt.id === operation.afterId
        );
        const position = beforeIndex === -1 ? afterIndex : beforeIndex + 1;
        const newCrdt = {
          id: operation.characterId,
          character: operation.character,
          beforeId: operation.beforeId,
          afterId: operation.afterId,
        };
        setContent([
          ...content.slice(0, position),
          newCrdt,
          ...content.slice(position),
        ]);
      } else if (operation.type === "deleteCharacter") {
        setContent(content.filter((crdt) => crdt.id !== operation.characterId));
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
    // send operation to server
    operationType,
    position,
    beforeId,
    afterId,
    character,
    characterId
  ) => {
    if (socket) {
      const data = {
        documentId: documentId,
        operation: {
          type: operationType,
          position: position,
          beforeId: beforeId,
          afterId: afterId,
          character: character,
          characterId: characterId,
        },
      };

      const jsonData = JSON.stringify(data);
      socket.send(jsonData);
    }
  };
  const editor = useEditor({
    extensions: extensions,
    content: content,
    onUpdate: ({ editor }) => {
      const newContent = editor.getText();
      const diff = getDiff(content, newContent); // need to implement this function
      if (diff) {
        console.log("Diff= ", diff);
        if (diff.type === "insert") {
          sendContentToServer(
            "insertCharacter",
            diff.position,
            diff.beforeId,
            diff.afterId,
            diff.character,
            diff.characterId
          );
        } else if (diff.type === "delete") {
          sendContentToServer(
            "deleteCharacter",
            diff.position,
            diff.beforeId,
            diff.afterId,
            diff.character,
            diff.characterId
          );
        }
      }
      setContent(newContent);
    },
  });
  useEffect(() => {
    console.log("Sssssssssssssssssss");
    if (editor) {
      console.log("Sssssssssssssss");
      editor.commands.setContent(content);
    }
  }, [content]);

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
