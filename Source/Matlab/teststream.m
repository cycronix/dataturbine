% test subscribe

function teststream(host, modeflag)

  if(nargin < 1) host = 'localhost';
  end
  if(nargin < 2) modeflag = 1;
  end
  
% Sink
  snk = rbnb_sink(host,'mySink');
  cmapsnk = rbnb_cmap;
%  cmapsnk.Add('mySource/x');
  cmapsnk.Add('mySource/*');
  
  if(modeflag == 0) snk.Monitor(cmapsnk, 1);
  else              snk.Subscribe(cmapsnk)
  end
  
% Source
  maxwait = 10;
  ncache = 20;
  ngot = 0;
  nloop = ncache;
  src = rbnb_source(host, 'mySource', ncache, 'none', 0);
  
%   pause(.1)    % let Sink notice source?
  
  for i=1:nloop
    rbnb_put(src,'x',i, i, 1);
    pause(0.01)  % this should not be nec with sync'd put
%    if(i==1) pause(.5)  % grope
%    end

    for j=1:maxwait          % avoid forever loop
        cmapsnk = snk.Fetch(200);
        if(cmapsnk.NumberOfChannels > 0) break;
      end
      %fprintf('oops no data\n');
    end
    if(j >= maxwait) 
        warning('Gave up waiting to fetch data');
        continue;
    else
        ngot = ngot + 1;
    end
    
    gdat = cmapsnk.GetDataAsFloat64(0);
    gtim = cmapsnk.GetTimes(0);
    gnam = cmapsnk.GetName(0);
  
    if(strcmp(gnam, 'mySource/x') ~= 1) 
      error('Channel name mismatch');
    end
    if(size(gdat) < 1)
      error('Fetched wrong amount of data');
    end
    if(gdat ~= gtim)    % we put in matching data=time

      error('Channel data mismatch');
    end
    if(gdat ~= i)    % we put in matching data=i
      i
      error('Channel data mismatch');
    end
  end
  
% all done with RBNB access
  src.CloseRBNBConnection;
  snk.CloseRBNBConnection;
  
% fprintf('ngot: %g, nloop: %g, fraction: %g\n',ngot,nloop,ngot/nloop);
  if(ngot < nloop) 
    wtxt = sprintf('Lost %d/%d data frame(s)\n', nloop-ngot, nloop);
    if(ngot/nloop < .5) error(wtxt)
    else                fprintf(wtxt)
    end

  end

  if(modeflag == 0) fprintf('PASS: monitor stream test\n');
  else              fprintf('PASS: subscribe stream test\n');
  end
  
  pause(.2);    % make sure its closed (close should be synchronous)
return
