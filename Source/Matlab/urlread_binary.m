% binary URL reader

function dat = urlread_binary(url, precision)

    if(nargin < 2) precision = 'single';
    end

    tempfile = 'foo.tmp';       % temporary file 

    urlwrite(url, tempfile);    % read URL, write to file

% cast URL result from string to binary    
    fid = fopen(tempfile,'r');
    dat = fread(fid, precision);
    fclose(fid);
    
    delete(tempfile);
    
return
    
   
    

