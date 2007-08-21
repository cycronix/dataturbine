% rbnb simple put/get test using utility functions
% multi-channel put

function [status] = testmulti(host)

  if(nargin < 1) host = 'localhost';
  end
  
  src = rbnb_source(host, 'mySource');
  snk = rbnb_sink(host,'mySink');
  
  % Source
  
  c1 =1:10;
  c2 = 2 * c1;
  c3 = 3*c1;
  c4 = 4*c1;

  cm = rbnb_cmap;
  i1=cm.Add('c1');
  i2=cm.Add('c2');
  i3=cm.Add('c3');
  i4=cm.Add('c1/c4');
  
  cm.PutTime(1,10);  % this applies to all putdata until next time-set
  
  cm.PutDataAsFloat64(i1,c1);
  cm.PutDataAsFloat64(i2,c2);
  cm.PutDataAsFloat64(i3,c3);
  cm.PutDataAsFloat64(i4,c4);
  
  src.Flush(cm,1);
  
  % Sink
  cm = rbnb_cmap;
  i1=cm.Add('mySource/c1');     % would be nice if could re-use put cmap,
  i2=cm.Add('mySource/c2');     % but need Source/chan on fetch (!)
  i3=cm.Add('mySource/c3');
  i4=cm.Add('mySource/c1/c4');
  
  snk.Request(cm, 0, 10, 'oldest');
  cmg = snk.Fetch(1000);

  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection(0,0);
  
  % Check Results
  
  c1g = cmg.GetDataAsFloat64(0)';
  d = c1g - c1;
  if(max(abs(d)) > 0)
    error('FAIL: testmulti/data values dont check');
  end
  
  t = cmg.GetTimes(0)';
  d = (1:10) - t;
  if(max(abs(d)) > 0) 
    error('FAIL: testmulti/time values dont check');
  end
  
  fprintf('PASS: testmulti\n');

  return
