# Tests that we can still distinguish the if case despite our workaround for
# the 'bug' in the AST
x = 42
print x # blastoff

t = [i for i in "Hello" if x]

print t
print t[0]
print t[1]
