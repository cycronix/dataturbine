% V2.1

function testuser(host)

  if(nargin < 1) host = 'localhost';
  end
  
  % Source
  
  src = rbnb_source(host, 'mySource');
  cmap = rbnb_cmap;
  ix  = cmap.Add('x');
  is  = cmap.Add('x/scale');
  iu  = cmap.Add('x/units');
  
  cmap.PutTime(0,1);                % initial time
 
  x1 = 1:5;
  cmap.PutDataAsFloat64(ix, x1);
  xs1 = 11:15;
  cmap.PutDataAsFloat64(is, xs1);
  xu = 'bananas';
  cmap.PutDataAsString(iu, xu);
  
  cmap.PutTime(1,1);                 % another time step
  x2 = x1 * 2;
  cmap.PutDataAsFloat64(ix, x2);
  xs2 = xs1 * 2;
  cmap.PutDataAsFloat64(is, xs2);
  
  src.Flush(cmap, 1);               % send it over
  
% Sink
  
  snk = rbnb_sink(host,'mySink');
  snk.GetChannelList('...');                % see what's there
  
  cmap.Clear;                       % re-use cmap
  cmap.Add('mySource/x');
  cmap.Add('mySource/x/scale');
  cmap.Add('mySource/x/units');
  
  snk.Request(cmap, 0, 2, 'oldest');
  cmap = snk.Fetch(1000);           % re-use cmap
    
  src.CloseRBNBConnection;
  
  ngot = cmap.NumberOfChannels;
  if(ngot ~= 3) error('Got wrong number of channels');
  end

  for i=0:(ngot-1)           % spot check results 
      
      d = rbnb_getdata(cmap,i);
      n = cmap.GetName(i);
      t = cmap.GetTimes(i);
      
      if(strcmp(n, 'mySource/x')) 
          if(max(abs(d-[x1 x2])) > 0) 
              error('chanx data mismatch');
          end
      end
      
      if(strcmp(n, 'mySource/x/scale')) 
          if(max(abs(d-[xs1 xs2])) > 0) 
              error('chanx/s data mismatch');
          end
      end
      
      if(strcmp(n, 'mySource/x/units')) 
          if(t ~= 0) 
              error('chanu time error');
          end
          if(strcmp(d,'bananas') ~= 1) 
              error('chanu data mismatch');
          end
      end
  end
  
  fprintf('PASS: user data test\n');

  snk.CloseRBNBConnection;

  return
