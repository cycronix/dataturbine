% simple test open/close

function [status] = testopen(host)

  if(nargin < 1) host = 'localhost';
  end
  
  clientname = 'testopen';

  % source
  src = javaObject('com.rbnb.sapi.Source');
  src.OpenRBNBConnection(host,clientname);
  
  checkname = src.GetClientName;
  if(strcmp(checkname, clientname) == 0) 
      error('testopen: unexpected client name');
  end

  src.GetServerName;    % just for fun
  
  src.CloseRBNBConnection(0, 0);
  
  % sink
  snk = javaObject('com.rbnb.sapi.Sink');
  snk.OpenRBNBConnection(host,clientname);

  % ask for Registration map a couple ways
  cmap = javaObject('com.rbnb.sapi.ChannelMap');
  cmap.Add('_Log/...');
  snk.RequestRegistration(cmap); % request just Log chans
  snk.Fetch(10000);

  snk.RequestRegistration;       % request all chans
  snk.Fetch(10000);
  
  snk.CloseRBNBConnection(0, 0);
  
  fprintf('PASS: open/close test\n');
return


