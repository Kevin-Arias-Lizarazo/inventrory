let accessToken = null;
let csrf = null;

export function setAccessToken(token) {
  accessToken = token;
}

export function getAccessToken() {
  return accessToken;
}

export function clearAccessToken() {
  accessToken = null;
}

export function setCsrfToken(token) {
  csrf = token;
}

export function getCsrfToken() {
  return csrf;
}

export function csrfToken() {
  return csrf;
}