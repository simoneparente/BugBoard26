const target = process.env.API_TARGET || 'http://localhost:8080';

module.exports = {
  '/api': {
    target: target,
    secure: false,
  },
};
