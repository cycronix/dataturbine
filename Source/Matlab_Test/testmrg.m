% V2.2
% rbnb archive merged filtering test using local rbnb and utility functions
% note: this .m file creates its own rbnb server and then a set of
% connections that are multithreaded.  The idea is to test the server's
% ability to properly handle multiple archives and multiple sinks accessing
% those archives.  This should test both the read/write locking and the
% fileset access capabilities of the server.
% The server must be started with '-Dnoreframe'.

function [status] = testmerger(host,nlines,nfilters,nchannels,nframes,timeout,debugit)

  if (nargin < 1)
    host = 'localhost';
  end
  if (nargin < 2)
    nlines = 3;
  end
  if (nargin < 3)
    nfilters = 2;
  end
  if (nargin < 4)
    nchannels = 3;
  end
  if (nargin < 5)
    nframes = 100;
  end
  if (nargin < 6)
    timeout = 90000;
  end
  if (nargin < 7)
    debugit = false;
  end

  mySources = javaArray('TestSource',nlines);
  myJoiners = javaArray('JoinInALines',nlines);
  myInALines = javaArray('InALine',nlines,nchannels);
  for idx=1:nlines
    mySources(idx) = TestSource;
    mySources(idx).setRBNBAddress(host);
    mySources(idx).setSourceName(sprintf('S_%d',idx));
    mySources(idx).setNumberOfChannels(nchannels);
    mySources(idx).setNumberOfFrames(nframes);
    mySources(idx).debug = debugit;
    mySources(idx).start;
    mySources(idx).join;

    for idx1=1:nchannels
      myInALines(idx,idx1) = InALine(host,mySources(idx),idx1 - 1,sprintf('IAL_%d_%d',idx,idx1),nfilters,nframes,timeout);
      myInALines(idx,idx1).debug = debugit;
      myInALines(idx,idx1).init;
    end

    myJoiners(idx) = JoinInALines(host,sprintf('J_%d',idx),myInALines(idx),nframes,timeout);
    myJoiners(idx).debug = debugit;
    myJoiners(idx).init;
  end

  merger = Merger(host,'M',myJoiners,nframes,timeout);
  merger.debug = debugit;
  merger.init;
  for idx=1:nlines
    mySources(idx).close(false,true);
    lSources(idx) = rbnb_source(host,sprintf('S_%d',idx),10,'append',nframes);
  end
  for idx=1:nlines
    for idx1=1:nchannels
      myInALines(idx,idx1).start;
    end
    myJoiners(idx).start;
  end
  merger.start;
  merger.join;
  merger.terminate;
  for idx=1:nlines
    myJoiners(idx).terminate;
    for idx1=1:nchannels
      myInALines(idx,idx1).terminate;
    end
    lSources(idx).CloseRBNBConnection(false,false);
  end

  for idx=1:nlines
    stat = mySources(idx).getStatus;
    if stat ~= 0
      message = sprintf('FAILED: Merger test - source %d error %d\n',idx,stat);
      break;
    end
    if stat ~= 0
      break;
    end
    for idx1=1:nchannels
      stat = myInALines(idx,idx1).getStatus;
      if stat ~= 0
        message = sprintf('FAILED: Merger test - in-a-line %d/%d error %d\n',idx,idx1,stat);
        break;
      end
    end
    if stat ~= 0
      break;
    end
    stat = myJoiners(idx).getStatus;
    if stat ~= 0
      message = sprintf('FAILED: Merger test - join-in-a-lines %d error %d\n',idx,stat);
      break;
    end
  end
  if stat == 0
    stat = merger.getStatus;
    if stat ~= 0
      message = sprintf('FAILED: Merger test - merger error %d\n',stat);
    else
      message = sprintf('PASS: Merger test');
    end
  end

  if stat ~= 0
    error(message);
  else
    fprintf(message);
  end
return
