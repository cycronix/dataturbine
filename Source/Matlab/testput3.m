% V2.1
% rbnb simple put/get test using utility functions

function [status] = testput3(host)

  if(nargin < 1) host = 'localhost';
  end

  src = rbnb_source(host, 'mySource');
  snk = rbnb_sink(host,'mySink');
  
  x=1:10;
  rbnb_put(src, 'x', x, 1, 10);
  
  [y t] = rbnb_get(snk, 'mySource/x', 1, 10);

  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection(0,0);
  
  % Check Results
  
  d = x - y;
  if(max(abs(d)) > 0)
    error('FAIL: testput3/data values dont check');
  end
  
  d = x - t;
  if(max(abs(d)) > 0) 
    error('FAIL: testput3/time values dont check');
  end
  
  fprintf('PASS: testput3\n');

  return
