const puppeteer = require('puppeteer');
const fs = require('fs');


async function getCookies() {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();

    await page.goto('https://potniski.sz.si');


    const cookies = await page.cookies();
  
    await browser.close();
    fs.writeFileSync('cookies.json', JSON.stringify(cookies));
    //console.log('Cookies:', cookies);
    return cookies;
}

getCookies().then(cookies => {
    console.log('Retrieved cookies:', cookies);
}).catch(error => {
    console.error('Error retrieving cookies:', error);
});
