% test route (presumes two routed DataTurbines exist)

function testroute(host1, host2)
  
%  global src snk
  
  if(nargin < 1) 
      srchost = 'localhost:4000';
      snkhost = 'localhost:5000';     % routed server    
  end
  
  sname = 'mySource';
  cname = 'x';
  
  src = rbnb_source(srchost,sname);
  
  x = 1:1000000;
  rbnb_put(src, cname, x, 0, 1);
  src.GetList;
  
  snk = rbnb_sink(snkhost,'mySink');
  snk.GetList;
  
% NEED syntax to find out local, parent, child server names
% need syntax to specify "up". i.e. ".."
  
%  gpath = '/fly5000/shortcuttofly3/fly4000';
  gpath = '/fly3333/fly4000';
  gname = [gpath '/' sname '/' cname];

  [y, tim, nam] = rbnb_get(snk, gname, 0, 1);

  src.CloseRBNBConnection(0,0);
  snk.CloseRBNBConnection(0,0);
  
  d = x - y;
  if(max(abs(d)) > 0) 
    error('FAIL: testroute/data values dont check');
  end

  fprintf('PASS: routing test\n');

return
  

