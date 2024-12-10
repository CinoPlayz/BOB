import { createContext } from 'react';

 export const UserContext = createContext({
     user: null,
     token: null,
     setUserContext: () => {},
     login: () => {},
     verifyTOTP: () => {},
     clickedEnable2fa: false,
     twoFaEnabled: false,
     setTwoFaEnabled: false,
     setClickedEnable2fa: false,
 });

