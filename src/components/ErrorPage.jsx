import React from 'react';
import errorImage from '../assets/error.png';

function ErrorPage({ message, error }) {
  return (
    <div id="errorTemplate" className="container mx-auto px-4 py-8">
      <div className="flex justify-center items-center">
        <div className="w-full md:w-1/2">  {/* Responsive width adjustment */}
          <div className="text-center">
            <img src={errorImage} alt="Error" className="w-full" /> {/* Image responsive */}
            {error && (
              <>
                <h1 className="text-3xl font-bold mb-4">{message || 'Error'}</h1>  {/* Adjusted heading styles */}
                <h2 className="text-xl mb-2">{error.status}</h2> {/* Adjusted subheading styles */}
                <pre className="text-sm bg-gray-100 p-2 rounded">{error.stack}</pre> {/* Code formatting */}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ErrorPage;
