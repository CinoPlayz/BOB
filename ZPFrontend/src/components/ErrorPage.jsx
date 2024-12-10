import React from 'react';
import errorImage from '../assets/error.png';

function ErrorPage({ message, error }) {
  return (
    <div id="errorTemplate" className="container mx-auto px-4 py-8">
      <div className="flex justify-center items-center">
        <div className="w-full md:w-1/2">
          <div className="text-center">
            <img src={errorImage} alt="Error" className="w-full" />
            {error && (
              <>
                <h1 className="text-3xl font-bold mb-4">{message || 'Error'}</h1>
                <h2 className="text-xl mb-2">{error.status}</h2>
                <pre className="text-sm bg-gray-100 p-2 rounded">{error.stack}</pre>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ErrorPage;
