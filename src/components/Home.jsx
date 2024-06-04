import React from 'react';
import FrontPhoto from '../assets/front.png';
import '../style/Home.css';

function Home() {
    return (
        <div className="home-container">
            <div className="left-side">
                <img src={FrontPhoto} alt="Front" className="overlapping-image" />
            </div>
            <div className="right-side">
                <p>BandOfBytes</p>
            </div>
            {/* <Footer /> */} {/* Included globally in App.jsx */}
        </div>
    );
}

export default Home;
