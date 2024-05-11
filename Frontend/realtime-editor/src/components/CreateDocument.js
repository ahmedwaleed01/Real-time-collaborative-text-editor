import React from "react";
import { Modal, Button } from "react-bootstrap";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import axios from "axios";
import { useContext } from "react";
import { AuthContext } from "./AuthProvider";
import {} from "./mainfeed.js";
function CreateDocumentModal({ show, handleClose, onCreate }) {
  const initialValues = {
    documentName: "",
    type: "",
  };

  const validationSchema = Yup.object({
    documentName: Yup.string().required("Document name is required"),
  });

  const { isAuthenticated, logout } = useContext(AuthContext); // was missing
  async function handleSubmit(values, { setSubmitting }) {
    console.log(values);
    try {
      const token = localStorage.getItem("token");
      const res = await axios.post(
        process.env.REACT_APP_API_URL + "/dc/create",
        {
          title: values.documentName,
          content: "",
          type: values.type,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      console.log("Document created:", res.data);
      // fetchDocuments();
      onCreate(values.documentName);

      window.location.href = "/";
    } catch (error) {
      console.error("Error:", error);
    }
    setSubmitting(false);
  }
  return (
    <Modal show={show} onHide={handleClose} centered>
      <Modal.Header closeButton>
        <Modal.Title>Create New Document</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Formik
          initialValues={initialValues}
          validationSchema={validationSchema}
          onSubmit={handleSubmit}
        >
          <Form>
            <div className="mb-3">
              <label htmlFor="documentName" className="form-label">
                Document Name
              </label>
              <Field type="text" name="documentName" className="form-control" />
              <ErrorMessage
                name="documentName"
                component="div"
                className="text-danger"
              />
            </div>
            <div className="mb-3">
              <label htmlFor="type" className="form-label">
                Document Type
              </label>
              <Field as="select" name="type" className="form-control">
                <option value="">Select type</option>
                <option value="private">Private</option>
                <option value="public">Public</option>
              </Field>
              <ErrorMessage
                name="type"
                component="div"
                className="text-danger"
              />
            </div>
            <Button type="submit" variant="primary">
              Create Document
            </Button>
          </Form>
        </Formik>
      </Modal.Body>
    </Modal>
  );
}

export default CreateDocumentModal;
