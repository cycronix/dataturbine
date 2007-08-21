% V2.2
% rbnb test checking adding data to an archive in "real-time"

function [status] = testa2d(host,nframes,nchans)

    if (nargin < 1)
	host = 'localhost';
    end

    if (nargin < 2)
	nframes = 120;
    end

    if (nargin < 3)
	nchans = 3;
    end

    if (nframes < 120)
	cframes = 30;
    else
	cframes = nframes/4;
    end
    aframes = nframes*100;

    src = rbnb_source(host,'mySource',cframes,'create',aframes);
    snk = rbnb_sink(host,'mySink');
    snkrmap = com.rbnb.sapi.ChannelMap;
    snkrmap.Add(strcat(char(src.GetClientName),'/...'));

    pframes = 0;
    gframes = 0;

    backup = 0;
    lframe = 0;
    while (gframes < nframes)
	if pframes == nframes/2
	    src.CloseRBNBConnection(false,true);
	    src = rbnb_source(host,'mySource',cframes,'append',aframes);
	    backup = max(1,nframes/10);
	end
	if lframe < nframes
	    for chan = 1:nchans
		name = strcat('c',num2str(chan));
		tdat = (pframes - backup)*nchans + chan;
		rbnb_put(src,name,tdat,tdat,1.);
		snk.RequestRegistration(snkrmap);
		snkfmap = snk.Fetch(-1);
	    end
	    pframes = pframes + 1;
	    if (pframes - backup) > lframe
		lframe = pframes - backup;
	    end
	end

	if pframes >= 3
	    for chan = 1:nchans
		name = strcat(char(src.GetClientName),'/c');
		name = strcat(name,num2str(chan));
		[dat,tim,nam] = rbnb_get(snk,name,0.,0.,'newest');
		tdat = (lframe - 1)*nchans + chan;
		if tim ~= tdat
		    src.CloseRBNBConnection(false,false);
		    snk.CloseRBNBConnection;
		    error('FAIL: wrong time retrieved');
		end
		if dat ~= tdat
		    src.CloseRBNBConnection(false,false);
		    snk.CloseRBNBConnection;
		    error('FAIL: wrong data retrieved');
		end
	    end
	    gframes = pframes;
	end
    end

    src.CloseRBNBConnection(false,false);
    snk.CloseRBNBConnection;

    fprintf('PASS: adding to newest data test\nNote: the server should produce exceptions about not being able to add\nframes because time is going backwards.\n');
return
