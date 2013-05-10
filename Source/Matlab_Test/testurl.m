 s = rbnb_source('localhost:8888','mysource')
 x=imread('rbnbChat.jpg');
 % ix = int8(x);
 ix = reshape(x,prod(size(x)),1,1);
 rbnb_put(s,'x.jpg',ix);
 s.Flush;
 

% s.CloseRBNBConnection(0,0)
  