const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');

const templatePath = path.resolve(__dirname, '../../../../clients/app/src/jsMain/resources/index.html');

config.plugins.push(new HtmlWebpackPlugin({
  template: templatePath,
  filename: 'index.html',
  inject: 'body',
}));
