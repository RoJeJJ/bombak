local gold = redis.call('hget',KEYS[1],'gold')
gold = tonumber(gold)
local subGold = tonumber(ARGV[1])

if(gold >= subGold) then
    redis.call('set',KEYS[1],gold - sub)
    return true
else
    return false
end