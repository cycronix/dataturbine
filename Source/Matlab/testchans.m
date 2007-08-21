% V2.2
% rbnb archive multiple channels test.
% This test will fail with pre V2.2 servers if the number of channels is
% greater than the number of filesets supported by the server (the default
% for older servers was 10).

function [status] = testchans(host,nchans)

  if (nargin < 1)
    host = 'localhost';
  end
  if (nargin < 2)
    nchans = 100;
  end

  src = rbnb_source(host,'mySource',10,'create',100);

  for idx=1:nchans
    rbnb_put(src,sprintf('c%d',idx),idx,idx,1);
  end
%disp('done putting data in');
  src.CloseRBNBConnection(0,1);
  src = rbnb_source(host,'mySource',10,'append',100);
  snk = rbnb_sink(host,'mySink');
%disp('reopened source, created sink');
  for idx=1:nchans
    [gdat gtim gnam] = rbnb_get(snk,sprintf('mySource/c%d',idx),idx,1);

    if (strcmp(gnam,sprintf('mySource/c%d',idx)) ~= 1)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Channel name mismatch');
    end

    if (length(gdat) < 1)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Channel data size mismatch');
    end

    if (gdat(1) ~= idx)
      src.CloseRBNBConnection(0,0);
      snk.CloseRBNBConnection;
      error('Channel data value mismatch');
    end
  end

  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection;

  fprintf('PASS: many channels test\n');
return
