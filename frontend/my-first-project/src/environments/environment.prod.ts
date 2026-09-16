// Environnement de production : URL surchargée au build via NG_APP_API_URL
// (le Dockerfile Angular la remplace par la valeur d'environnement).
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8089/gestiondestock'
};
