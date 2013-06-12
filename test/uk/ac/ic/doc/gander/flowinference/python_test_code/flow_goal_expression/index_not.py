x = 0
print x # blastoff

l = [1,2,3]
t = l[0] # x doesn't escape here because it isn't used as an index! Bad bug.

print t
print x # x flows here
