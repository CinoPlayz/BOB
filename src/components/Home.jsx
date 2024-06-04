import { useEffect, useContext } from 'react';
import Footer from './Footer';


function Home() {
    return (
        <div className="home-container bg-white dark:bg-gray-200">
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '89vh' }}>
                <p>BandOfBytes</p>
            </div>
            {/* <Footer /> */}
        </div>
    );
}

export default Home;