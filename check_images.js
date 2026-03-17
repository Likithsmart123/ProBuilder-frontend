const fs = require('fs');
const files = [
    'C:/Users/hp/AndroidStudioProjects/ProBuilder/app/src/main/res/drawable/img_bricks.png',
    'C:/Users/hp/AndroidStudioProjects/ProBuilder/app/src/main/res/drawable/img_cement.png',
    'C:/Users/hp/AndroidStudioProjects/ProBuilder/app/src/main/res/drawable/img_steel.png',
    'C:/Users/hp/AndroidStudioProjects/ProBuilder/app/src/main/res/drawable/img_paint.png',
    'C:/Users/hp/AndroidStudioProjects/ProBuilder/app/src/main/res/drawable/img_sand.png'
];

files.forEach(file => {
    try {
        const buffer = Buffer.alloc(8);
        const fd = fs.openSync(file, 'r');
        fs.readSync(fd, buffer, 0, 8, 0);
        fs.closeSync(fd);
        console.log(`${file}: ${buffer.toString('hex')}`);
    } catch (err) {
        console.error(`Error reading ${file}: ${err.message}`);
    }
});
