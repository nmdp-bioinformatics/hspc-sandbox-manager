var express = require("express");
var app     = express();
var path    = require("path");

app.use(express.static(path.join(__dirname + '/src')));
app.use(function (req, res, next) {
    res.header('Cache-Control', 'private, no-cache, no-store, must-revalidate');
    res.header('Expires', '-1');
    res.header('Pragma', 'no-cache');
    next()
});

app.get('*', function(req,res){
  res.sendFile(__dirname+'/src/index.html');
});

app.listen(8080);


