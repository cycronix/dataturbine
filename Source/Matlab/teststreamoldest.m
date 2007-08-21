% V3.0
% Regression test for the following bug:
% Subscribing to oldest to a Source that has registered channels where the
% Subscription is started before any data is in the Source has the following
% bugs:
% - for time-based Subscribe: the fetch returns immediately with no data and the Subscription is stopped
% - for frame-based Subscribe: each fetch will return *all* the data currently in the Source

function [status] = teststreamoldest(host,subscribeMode)

  if (nargin < 1)
    host = 'localhost';
    subscribeMode = 0;
  elseif (nargin < 2)
      subscribeMode = 0;
  end
  
  % Start a source and register channels
  src = rbnb_source(host,'testStreamSource',100,'none',0);
  cm = com.rbnb.sapi.ChannelMap;
  cm.Add('c1');
  cm.Add('c2');
  src.Register(cm);
  pause(1);
  
  % Start the Subscription before putting data into the Source
  snk = rbnb_sink(host,'testSink');
  cm = com.rbnb.sapi.ChannelMap;
  cm.Add(strcat(char(src.GetClientName),'/...'));
  if (subscribeMode == 0)
      % Frame-based Subscribe to oldest
      snk.Subscribe(cm,'oldest');
  else
      % Time-based Subscribe to oldest
      snk.Subscribe(cm,0,0,'oldest');
  end
  pause(1);
  
  % Put data into Source, fetch it with the Sinks
  for idx=1:5
      % Put in data
      dataPt = idx;
      cmap = com.rbnb.sapi.ChannelMap;
      cmap.Add('c1');
      cmap.Add('c2');
      cmap.PutTime(dataPt,0);
      pdata = [dataPt];
      cmap.PutDataAsFloat64(0,1*pdata);
      cmap.PutDataAsFloat64(1,2*pdata);
      src.Flush(cmap);
      
      if (subscribeMode == 0)
          % Fetch data - frame-based Subscribe
      	  tempcm = snk.Fetch(100);
          if (tempcm.NumberOfChannels == 0)
	      src.CloseRBNBConnection;
      	      snk.CloseRBNBConnection;
	      error('FAIL: (1) Frame-based subscribe to oldest failed (Source initially had no data but had registered chans).');
          end
          timearray = tempcm.GetTimes(0);
          [m,n] = size(timearray);
          if ( (m ~= 1) || (n ~= 1) || (timearray ~= idx) )
              src.CloseRBNBConnection;
              snk.CloseRBNBConnection;
      	      error('FAIL: (2) Frame-based subscribe to oldest failed (Source initially had no data but had registered chans).');
          end
      else
          % Fetch data - time-based Subscribe
          tempcm = snk.Fetch(100);
          if (tempcm.NumberOfChannels == 0)
	      src.CloseRBNBConnection;
      	      snk.CloseRBNBConnection;
	      error('FAIL: (3) Time-based subscribe to oldest failed (Source initially had no data but had registered chans).');
          end
          timearray = tempcm.GetTimes(0);
          [m,n] = size(timearray);
          if ( (m ~= 1) || (n ~= 1) || (timearray ~= idx) )
	      src.CloseRBNBConnection;
      	      snk.CloseRBNBConnection;
	      error('FAIL: (4) Time-based subscribe to oldest failed (Source initially had no data but had registered chans).');
          end
      end
  end
  
  src.CloseRBNBConnection;
  snk.CloseRBNBConnection;
  
  if (subscribeMode == 0)
      fprintf('PASS: frame-based streaming from oldest test (where Source initially had no data but had registered chans)\n');
  else
      fprintf('PASS: time-based streaming from oldest test (where Source initially had no data but had registered chans)\n');
  end
  
return

