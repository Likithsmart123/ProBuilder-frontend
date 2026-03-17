const fs = require('fs');
const file = 'C:/Users/hp/AndroidStudioProjects/ProBuilder/app/src/main/res/drawable/img_bricks.png';
try {
    const buffer = fs.readFileSync(file);
    console.log(`File size: ${buffer.length}`);
    console.log(`First 16 bytes: ${buffer.slice(0, 16).toString('hex')}`);
} catch (err) {
    console.error(err);
}
