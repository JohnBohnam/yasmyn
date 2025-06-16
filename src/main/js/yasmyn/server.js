const express = require('express');
const path = require('path');
const app = express();

const PORT = process.env.PORT || 8081;

// Serve static files from web-build
app.use(express.static(path.join(__dirname, 'web-build')));

app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'web-build', 'index.html'));
});

app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
