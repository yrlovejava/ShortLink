-- keys
--KEYS[1] 用户名

-- values
-- ARGV[1] 时间窗口大小，单位: 秒
-- 设置用户访问频率限制的参数

-- 构造 Redis 中存储用户访问次数的键名
local accessKey = "short-link:user-flow-risk-control:" .. KEYS[1]

-- 原子递增访问次数，并获取递增后的值
local currentAccessCount = redis.call("INCR",accessKey)

-- 第一次设置键的时候设置过期时间
if currentAccessCount == 1 then
   redis.call("EXPIRE",accessKey,tonumber(ARGV[1]))
end

-- 返回当前返回次数
return currentAccessCount