import { Components } from "components/Components";
import { LoginInfo } from "model/Model";

export const UserLoginPOST = () => {
  ////////////// 입력해야하는 부분 ///////////
  const API = {
    uri: "/user/login",
    method: "POST",
    detail: "로그인",
    completed: false,
  };
  const requestBody = {
    LoginInfo,
  };
  ///////////////////////////////////

  return (
    <>
      <Components API={API} requestBody={requestBody} />
    </>
  );
};
