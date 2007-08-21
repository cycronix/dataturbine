% V2.1
% simple put data test

function [status] = testput2(host)

  if(nargin < 1) host = 'localhost';
  end

  % Source
  
  src = rbnb_source(host, 'mySource');
  cmp = rbnb_cmap;
  idx1 = cmp.Add('x');
  idx2 = cmp.Add('y');
  idx3 = cmp.Add('z');

  % put in point by point, individual point time stamps
  for i=1:10
      cmp.PutTime(i,1.);        % PutTime(start,duration), implied chanidx
      cmp.PutDataAsFloat64(idx1, i);
      cmp.PutDataAsFloat64(idx2, i*2);
      cmp.PutDataAsFloat64(idx3, i*3);
%      src.Flush(cmp,1);
  end
  
  src.Flush(cmp,1);
  
  % Sink
  
  snk = rbnb_sink(host,'mySink');
  snk.GetChannelList('...');
  cmp.Clear;                        % re-use chanMap
  cmp.Add('mySource/x');
  cmp.Add('mySource/y');
  cmp.Add('mySource/z');

  snk.Request(cmp, 0., 10., 'oldest');
  cmp = snk.Fetch(1000);            % re-use chanMap
  if(cmp.NumberOfChannels < 1) 
    error('FAIL: testput2/ngot is less than 1');
  end

  idx = cmp.GetIndex('mySource/x');
  if(idx ~= idx1)
      error('FAIL:  testput2 unexpected channel index')
  end
  cmp.GetName(idx);
  if(strcmp(cmp.GetName(idx1),'mySource/x') ~= 1)
      error('FAIL:  testput2 fetched channel name is wrong')
  end

  y = cmp.GetDataAsFloat64(idx);
  y = y';   % ack y is returned as column vector, need row vector !
  
  t = cmp.GetTimes(0);
  t = t';   % ack
  
  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection(0,0);
  
  % Check Results
  
  d = (1:10) - y;
  if(max(abs(d)) > 0) 
    error('FAIL: testput2/data values dont check');
  end
  
  d = (1:10) - t;
  if(max(abs(d)) > 0) 
    error('FAIL: testput2/time values dont check');
  end
  
  fprintf('PASS: testput2\n');

  return

