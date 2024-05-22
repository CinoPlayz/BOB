import { createContext } from 'react';

 export const UserContext = createContext({
     user: null,
     token: null,
     setUserContext: () => {},
     login: () => {}
 });