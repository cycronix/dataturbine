% V2.1
% simple put data test

function testput1(host)
  
  if(nargin < 1) host = 'localhost';
  end
  
  NPTS=10000; %10000;
  
  src = rbnb_source(host,'mySource',NPTS/2,'create',NPTS);
  cmapsrc = rbnb_cmap;

 idx = cmapsrc.Add('x');

  x = 1:(NPTS-0);
  
  for(i=x)
    cmapsrc.PutTime(i,0)
    cmapsrc.PutDataAsFloat64(idx,i);
    src.Flush(cmapsrc, 1);
  end 
  
  snk = rbnb_sink(host,'mySink');
  snk.GetChannelList;                      % let's see what's available
  cmapreq = rbnb_cmap;
  cmapreq.Add('mySource/x');
   tic;  
  snk.Request(cmapreq, 3, 0, 'absolute');
  cmapget = snk.Fetch(60000);
  fprintf('abs 0 dur fetch time: %g\n',toc);

  
   tic;
  snk.Request(cmapreq, 0, NPTS/2, 'newest');
  cmapget = snk.Fetch(60000);
  fprintf('newest fetch time: %g\n',toc)
  
   tic;
  snk.Request(cmapreq, 0, NPTS, 'newest');
  cmapget = snk.Fetch(60000);
  fprintf('all fetch time: %g\n',toc)
  for i=1:0
  tic;
  snk.Request(cmapreq, 0, NPTS/2, 'oldest');
  cmapget = snk.Fetch(60000);
  fprintf('oldest fetch time: %g\n',toc)

  tic;
  snk.Request(cmapreq, 0, NPTS/2, 'newest');
  cmapget = snk.Fetch(60000);
  fprintf('newest fetch time: %g\n',toc)

  tic;
  snk.Request(cmapreq, 0, NPTS/2, 'oldest');
  cmapget = snk.Fetch(60000);
  fprintf('oldest fetch time: %g\n',toc)
  
  tic;
  snk.Request(cmapreq, 0, NPTS/2, 'newest');
  cmapget = snk.Fetch(60000);
  fprintf('newest fetch time: %g\n',toc)
  end  
  %for i=1:250:10000
  %tic;
  %snk.Request(cmapreq, i, 0, 'absolute');
  %cmapget = snk.Fetch(60000);
  %fprintf('%d fetch time: %g\n',i,toc);
  %end
  
  tic;
  for (i=1:0)
  snk.Request(cmapreq, NPTS/4 + i*3, 0, 'absolute');
  cmapget = snk.Fetch(60000);

  fprintf('absolute fetch time: %g\n',toc/100)
  
  tic;  
  snk.Request(cmapreq, 3000, 0, 'absolute');
  cmapget = snk.Fetch(60000);
  fprintf('abs 0 dur fetch time: %g\n',toc);
  end  
 
  if(cmapget.NumberOfChannels < 1) 
    error('FAIL: testput1 snk.Fetch failed');
  end

  if(cmapget.GetType(0) ~= cmapget.TYPE_FLOAT64)
    error('FAIL: testput1 got wrong datatype')
  end
    
  y = cmapget.GetDataAsFloat64(0)';
  
  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection(0,0);

  d = x - y;
  if(max(abs(d)) > 0) 
    error('FAIL: testput1/data values dont check');
  end

  fprintf('PASS: testput1\n');

  return

