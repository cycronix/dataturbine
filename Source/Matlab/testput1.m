% V2.1
% simple put data test

function testput1(host)
  
  if(nargin < 1) host = 'localhost';
  end
  
  src = rbnb_source(host,'mySource');
  cmapsrc = rbnb_cmap;
%  cmapsrc.PutTime(0,10);
  cmapsrc.PutTimeAuto('timeofday');
 
  idx = cmapsrc.Add('x');
  cmapsrc.PutUserInfo(idx,'units=foo,scale=10,offset=-10');
  src.Register(cmapsrc);

  x = 0:10;
  cmapsrc.PutDataAsFloat64(idx,x);
 
  src.Flush(cmapsrc, 1);

%  pause
  snk = rbnb_sink(host,'mySink');
  snk.GetChannelList;                      % let's see what's available
  
  cmapreq = rbnb_cmap;
  cmapreq.Add('mySource/x');
  snk.Request(cmapreq, 0., 1., 'oldest');
  
  cmapget = snk.Fetch(1000);

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

