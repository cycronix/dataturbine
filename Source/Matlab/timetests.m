% timetest for range of payloads


payload = [ 10 20 50 100 200 500 1000 2000 5000 10000 20000 50000 100000 ];

for i=1:length(payload)
    t(i) = timetest(payload(i));
    t(i)
end

t

loglog(payload, t)
xlabel('Data Payload (Bytes)')
ylabel('Data Rate (Bytes/Sec)')
title('RBNB Data Rate Vs Frame Size')