x = 0
print x # blastoff

l = [1,2,3]
t = l[x] # x escapes here because we aren't sure that it doesn't flow to __getitem__

print t
