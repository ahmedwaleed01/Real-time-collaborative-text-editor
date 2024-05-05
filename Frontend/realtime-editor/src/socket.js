import { io } from "socket.io-client";

// "undefined" means the URL will be computed from the `window.location` object
const URL = "http://51.103.213.89:8000";

export const socket = io(URL);
