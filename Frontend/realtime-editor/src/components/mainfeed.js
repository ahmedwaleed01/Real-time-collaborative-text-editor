import React, { useState, useContext } from "react";
import DocumentList from "./DocumentList";
import CreateDocumentForm from "./CreateDocument";
import { Button } from "react-bootstrap";
import TextEditor from "./textEditor";
import { AuthContext } from "./AuthProvider";
import Login from "./login";
import { useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function App() {
  const navigate = useNavigate();
  const [ownedDocuments, setOwnedDocuments] = useState([]);
  const [sharedDocuments, setSharedDocuments] = useState([]);
  const { isAuthenticated, logout } = useContext(AuthContext);
  const [modalShow, setModalShow] = useState(false);

  async function handleShareRequest(selectedDocId, username, permission) {
    const token = localStorage.getItem("token");
    console.log("aaaaaa", selectedDocId, username, permission);
    if (token) {
      try {
        const res = await axios.post(
          `http://51.103.213.89/dc/share/${selectedDocId}`,
          {
            permission: permission.toUpperCase(),
            username: username,
          },
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        console.log("Fetched shared:", res.data);
      } catch (error) {
        if (error.response.status === 403) {
          alert("Your Session expired. Logging out...");
          logout();
        } else {
          console.error("Error fetching owned documents:", error);
        }
      }
    }
  }

  async function fetchOwnedDocuments() {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const res = await axios.get(`http://51.103.213.89/dc/viewAll`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        console.log("Fetched owned documents:", res.data);
        // Update your state with the fetched documents here
        setOwnedDocuments(
          res.data.map((doc) => ({
            id: doc.id,
            title: doc.title,
            content: doc.content,
          }))
        );
      } catch (error) {
        if (error.response.status === 403) {
          alert("Your Session expired. Logging out...");
          // console.error("Token expired. Logging out...");
          logout();
        } else {
          console.error("Error fetching owned documents:", error);
        }
      }
    }
  }
  async function fetchSharedDocuments() {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const res = await axios.get(`http://51.103.213.89/dc/viewShared`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        console.log("Fetched shared documents:", res.data);
        setSharedDocuments(
          res.data.map((doc) => ({
            id: doc.id,
            title: doc.title,
            content: doc.content,
          }))
        );
      } catch (error) {
        if (error.response.status === 403) {
          alert("Your Session expired. Logging out...");
          // console.error("Token expired. Logging out...");
          logout();
        } else {
          console.error("Error fetching owned documents:", error);
        }
      }
    }
  }

  const handleOpenModal = () => setModalShow(true);
  const handleCloseModal = () => setModalShow(false);

  const handleCreate = (documentTitle) => {
    const newDocument = { id: Date.now(), title: documentTitle };
    setOwnedDocuments([...ownedDocuments, newDocument]);
    fetchOwnedDocuments();
  };
  async function handleDelete(documentId) {
    const token = localStorage.getItem("token");

    try {
      const res = await axios.delete(
        `http://51.103.213.89/dc/delete/${documentId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      console.log("Document deleted:", res.data);
      // Update your state with the deleted document here
      setOwnedDocuments(ownedDocuments.filter((doc) => doc.id !== documentId));
    } catch (error) {
      console.error("Error deleting document:", error);
    }
  }

  async function handleRename(documentId, newName) {
    const token = localStorage.getItem("token");

    try {
      const res = await axios.put(
        `http://51.103.213.89/dc/rename/${documentId}`,
        { title: newName },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      console.log("Document renamed:", res.data);
      // Update your state with the renamed document here
      setOwnedDocuments((docs) =>
        docs.map((doc) =>
          doc.id === documentId ? { ...doc, title: newName } : doc
        )
      );
    } catch (error) {
      console.error("Error renaming document:", error);
    }
  }

  const handleOpen = async (documentId) => {
    navigate(`/dc/view/${documentId}`);
  };
  useEffect(() => {
    // Fetch owned documents when component mounts
    fetchOwnedDocuments();
    fetchSharedDocuments();
  }, []);

  return (
    <>
      {!isAuthenticated ? (
        <Login />
      ) : (
        <div className="container">
          <div className="container mt-4">
            <h1 className="border-bottom border-dark mb-4 mt-2 pb-2">
              Dashboard
            </h1>
            <Button variant="primary" onClick={handleOpenModal}>
              Create New Document
            </Button>
            <CreateDocumentForm
              show={modalShow}
              handleClose={handleCloseModal}
              onCreate={handleCreate}
            />
          </div>
          <h2>Owned Documents</h2>
          <DocumentList
            documents={ownedDocuments}
            onDelete={handleDelete}
            onRename={handleRename}
            onShare={handleShareRequest}
            onOpen={handleOpen}
          />

          <h2>Shared With You</h2>
          <DocumentList
            documents={sharedDocuments}
            onDelete={handleDelete}
            onRename={handleRename}
            onShare={handleShareRequest}
            onOpen={handleOpen}
          />
        </div>
      )}
    </>
  );
}

export default App;
