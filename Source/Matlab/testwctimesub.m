% test wild-card time-subscription

function testwctimesub(sname)

  host = 'localhost';
  if(nargin < 1) sname = 'mySource';
  end
  
% Sink
  snk = rbnb_sink(host,'mySink');
  cmapsnk = rbnb_cmap;
  cmapsnk.Add([sname '/*']);    % wildcard channel spec

% Source
  src = rbnb_source(host,sname, 100, 'none', 0);
  
  snk.Subscribe(cmapsnk,0,0,'newest');      % time-based subscribe
%  snk.Subscribe(cmapsnk);          % frame-based subscribe
  
  pause(1)    % let Sink notice source?
    
% prime the pump with one channel, add new chans later
  cmap = rbnb_cmap;
  cmap.Add('zz');
  cmap.PutDataAsFloat64(0,-1);
  src.Flush(cmap,1);
%  pause(.1);
  cmapsnk = snk.Fetch(10000);
  cn = cmapsnk.GetName(0);
  cd = rbnb_getdata(cmapsnk, 0);
  if(cd ~= -1)
      snk.CloseRBNBConnection;
      src.CloseRBNBConnection;
      error('failed to get data for first channel');
  end
      
  for i=1:10          % avoid forever loop
        pause(.1)
        cmap = rbnb_cmap;
%        cmap.PutTime(i, 1);    % use auto TOD timestamp
        ix = cmap.Add('x1');
        cmap.PutDataAsFloat64(ix, i);
        ix = cmap.Add('x2');
        cmap.PutDataAsFloat64(ix, i+100);
        ix = cmap.Add('s');
        cstring = sprintf('hi %g',i);
        cmap.PutDataAsString(ix, cstring);  % mixed type
        nput = src.Flush(cmap, 1);
%        pause(.1) 
        
        cmapsnk = snk.Fetch(10000);
        nchan = cmapsnk.NumberOfChannels();
        if(nchan > 0) 
            for j=1:nchan
                cn = cmapsnk.GetName(j-1);
                cd = rbnb_getdata(cmapsnk, j-1);
                if(j==1)
                    if(cn ~= 's')   % will come back alphabetical
                        cn
                        snk.CloseRBNBConnection;
                        src.CloseRBNBConnection;
                        error('unexpected cname');
                    end
                    if(~strcmp(cd,cstring)) 
                        snk.CloseRBNBConnection;
                        src.CloseRBNBConnection;
                        error('unexpected data');
                    end
                end
            end
        else
            error('got no data');
        end
  end
  
    snk.CloseRBNBConnection;
    src.CloseRBNBConnection;
    
    fprintf('PASS: wildcard time-mode subscribe test\n');
return
