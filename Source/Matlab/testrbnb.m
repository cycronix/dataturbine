% ---------------------------------------------------------------------------
% testrbnb
%
% Description:	Test the function of the Matlab RBNB software.
%
% Usage:    	status = testrbnb(host)
% 
% Where:
%	host:  	    Host RBNB machine.  E.g. 'vixen.creare.com:3333'
%		        Default is localhost
%
% ---------------------------------------------------------------------------


function testrbnb(host)

  if(nargin < 1) host = 'localhost';
  end
  
  testopen   (host);
  testput1   (host);
  testput2   (host);
  testput3   (host);
  testmulti  (host);
  testmrb    (host);
  testuser   (host);
  testarc    (host);
  testzerod  (host);
  testzdnzt  (host);
  teststream (host, 0);
  teststream (host, 1);
  % JPW 03/14/2007: add teststreamoldest
  teststreamoldest(host, 0);
  teststreamoldest(host, 1);
  testarcsub (host);
  testtimesub(host);
  teststdp   (host);
  teststof   (host);
  testchans  (host);
  testa2c    (host);
  testextst  (host);
  testmaxw   (host);
  testdetach (host);
  testreattach(host);
  testpw     (host);
  
%  testarc2   (varargin{:});
%  testmulti  (varargin{:});
%  testscatter(varargin{:});
%  testtime   (varargin{:});
%  testpword  (varargin{:});
%  testwild   (varargin{:});
%  testmsg    (varargin{:});
%  testmrg    (varargin(:));
%  teststrold (host);

return


