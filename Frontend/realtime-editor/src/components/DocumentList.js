import React, { useState } from "react";
import { Card, Row, Col, Button } from "react-bootstrap";
import { MdDriveFileRenameOutline } from "react-icons/md";
import { MdDelete } from "react-icons/md";
import { FaRegShareSquare } from "react-icons/fa";
import { FaRegFolderOpen } from "react-icons/fa";
import Rename from "./Rename";

function DocumentList({ documents, onDelete, onRename, onShare, onOpen }) {
  const [isRenameVisible, setRenameVisible] = useState(false);
  const [selectedDocId, setSelectedDocId] = useState(null);

  // Function to handle renaming
  const handleRename = (newName) => {
    if (selectedDocId) {
      console.log(selectedDocId, newName);
      onRename(selectedDocId, newName);
      setRenameVisible(false); // Close the modal after renaming
    }
  };
  return (
    <Row>
      {documents.map((doc) => (
        <Col key={doc.id} sm={12} md={6} lg={3} className="mb-3">
          <Card>
            <Card.Title className="m-4">{doc.name}</Card.Title>
            <Card.Body
              className="d-flex flex-column justify-content-between h-100 mt-4"
              style={{ backgroundColor: "#DCDCDC" }}
            >
              <div className="mt-2">
                <Button
                  variant="danger"
                  size="sm"
                  className="mx-1 rounded-5 p-2 m-1"
                  onClick={() => onDelete(doc.id)}
                >
                  <MdDelete />
                  Delete
                </Button>
                <Button
                  variant="warning"
                  size="sm"
                  className="mx-1 rounded-5 p-2 m-1"
                  onClick={() => {
                    setSelectedDocId(doc.id);
                    setRenameVisible(true);
                  }}
                >
                  <MdDriveFileRenameOutline /> Rename
                </Button>
                <Rename
                  show={isRenameVisible}
                  handleClose={() => setRenameVisible(false)}
                  HandleSubmit={handleRename}
                />

                <Button
                  variant="info"
                  size="sm"
                  className="mx-1 rounded-5 p-2 m-1"
                  onClick={() => onShare(doc.id)}
                >
                  <FaRegShareSquare />
                  Share
                </Button>
                <Button
                  variant="primary"
                  size="sm"
                  className="mx-1 rounded-5 p-2 m-1"
                  onClick={() => onOpen(doc.id)}
                >
                  <FaRegFolderOpen /> Open
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      ))}
    </Row>
  );
}

export default DocumentList;