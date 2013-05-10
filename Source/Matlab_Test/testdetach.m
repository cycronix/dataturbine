% V2.2
% rbnb detach from server test.
% This only works with releases that support cache clearing.

function [status] = testdetach(host)

    if (nargin < 1)
	host = 'localhost';
    end

    % setup sink and source
    snk = rbnb_sink(host,'mySink');
    src = rbnb_source(host,'mySource',20,'create',1000);
    name = char(src.GetClientName);
    edata = 1:1000;
    sdata = edata*2;
    for i = 1:100
	edata = edata*2;
	rbnb_put(src,'c',edata,i,1);
    end

    % retrieve last data point from the cache
    [fdat,ftim,fnam] = rbnb_get(snk,strcat(name,'/c'),100,1);
    if fdat ~= edata
	src.CloseRBNBConnection(false,false);
	snk.CloseRBNBConnection;
        error('FAILED: data retrieved from connected source does not match expected');
    end

    % detach the source
    src.CloseRBNBConnection(true,true);

    % last data point should still be in the cache
    [fdat,ftim,fnam] = rbnb_get(snk,strcat(name,'/c'),100,1);
    if fdat ~= edata
	src.CloseRBNBConnection(false,false);
	snk.CloseRBNBConnection;
        error('FAILED: data retrieved from detached source does not match expected');
    end

    % clear the cache and detach
    src = rbnb_source(host,name,20,'append',1000);
    src.ClearCache;
    src.CloseRBNBConnection(true,true);

    % last data point should be in the archive
    [fdat,ftim,fnam] = rbnb_get(snk,strcat(name,'/c'),100,1);
    if fdat ~= edata
	src.CloseRBNBConnection(false,false);
	snk.CloseRBNBConnection;
        error('FAILED: data retrieved from cache-less source does not match expected');
    end

    % clear everything
    src = rbnb_source(host,name,20,'append',1000);
    src.CloseRBNBConnection(false,false);

    % there should be no data - rbnb_get would fail, so we request the data
    % via the standard Java simple API
    rmap = com.rbnb.sapi.ChannelMap;
    rmap.Add(strcat(name,'/c'));
    snk.Request(rmap,100,1,'absolute');
    cmap = snk.Fetch(-1);

    if cmap.NumberOfChannels ~= 0
	src.CloseRBNBConnection(false,false);
	snk.CloseRBNBConnection;
	error('FAILED: unexpectedly got data after close.');
    end

    snk.CloseRBNBConnection;
    fprintf('PASS: detach source test\n');

return
