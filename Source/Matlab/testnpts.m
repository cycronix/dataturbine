% V2.1
% simple put data test

function testnpts(NPTS,host)
  

  if (nargin < 1) NPTS=1000;
  end
  if(nargin < 2) host = 'localhost';
  end
  
  %src = rbnb_source(host,'mySource',NPTS/2,'create',NPTS);
  src = rbnb_source(host,'mySource',NPTS,'none',0);
  cmapsrc = rbnb_cmap;

 idx = cmapsrc.Add('x');

  x = 1:(NPTS-0);
  
  for(i=x)
    cmapsrc.PutTime(i,0)
    %cmapsrc.PutDataAsFloat64(idx,i);
    cmapsrc.PutDataAsByteArray(idx,i);
    src.Flush(cmapsrc, 1);
  end
  
%  pause
  snk = rbnb_sink(host,'mySink');
  snk.GetChannelList;                      % let's see what's available
  
  cmapreq = rbnb_cmap;
  cmapreq.Add('mySource/x');
  %disp('sleeping for 5 seconds before making requests');
  %pause(5);
  tic;
  for (i=1:100)
  snk.Request(cmapreq, NPTS/4 + i*3, 0, 'absolute');
  cmapget = snk.Fetch(60000);
  end
  fprintf('absolute fetch time: %g\n',toc/100)
  
  %snk.Request(cmapreq, 0, 5, 'newest');
  %tic;
  %cmapget = snk.Fetch(60000);
  %fprintf('newest fetch time: %g\n',toc)
  
  if(cmapget.NumberOfChannels < 1) 
    error('FAIL: testput1 snk.Fetch failed');
  end

  if(cmapget.GetType(0) ~= cmapget.TYPE_BYTEARRAY)
  %if(cmapget.GetType(0) ~= cmapget.TYPE_FLOAT64)
    error('FAIL: testput1 got wrong datatype')
  end
    
  y = cmapget.GetDataAsByteArray(0)';
  
  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection(0,0);

%  d = x - y;
%  if(max(abs(d)) > 0) 
%    error('FAIL: testput1/data values dont check');
%  end

  fprintf('PASS: testnpts\n');

  return

