% V2.5
% test password

function testpw(host)
  
  if(nargin < 1) host = 'localhost';
  end
  
  usr = 'myUsr';
  pw  = 'myPW';
  srcname = 'mySource';
  
  % SOURCE
  
  src = com.rbnb.sapi.Source(100, 'create', 1000);
  %src.OpenRBNBConnection(host, srcname);
  src.OpenRBNBConnection(host, srcname, usr, pw);
  
  srcname = src.GetClientName;  % may not get what ask for
  
  cmapsrc = rbnb_cmap;
  cmapsrc.PutTimeAuto('timeofday');
  
  x = 1:10;
  idx = cmapsrc.Add('x');
  cmapsrc.PutDataAsFloat64(idx,x);
 
  src.Flush(cmapsrc, 1);

  %src.Detach;
  src.CloseRBNBConnection;
  src = com.rbnb.sapi.Source(1, 'load', 0);
  src.OpenRBNBConnection(host, srcname, 'jj', pw);
  
  % SINK
  
  snk = com.rbnb.sapi.Sink;
  snk.OpenRBNBConnection(host, 'mySink', 'mm', pw);  % user name doesn't matter, only pw
  snk.GetChannelList;                                % can get list even with bad pw
  
  cmapreq = rbnb_cmap;
  snkchan = sprintf('%s/x',char(srcname));
  cmapreq.Add(snkchan);
  snk.Request(cmapreq, 0., 1., 'oldest');
  
  cmapget = snk.Fetch(1000);
 
  if(cmapget.NumberOfChannels < 1) 
    error('FAIL: testpw snk.Fetch failed');
  end

  if(cmapget.GetType(0) ~= cmapget.TYPE_FLOAT64)
    error('FAIL: testpw got wrong datatype')
  end
    
  y = cmapget.GetDataAsFloat64(0)';

  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection(0,0);
  
  d = x - y;
  if(max(abs(d)) > 0) 
    error('FAIL: testpw/data values dont check');
  end

  fprintf('PASS: password test\n');

  return

