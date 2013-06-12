# A single element AST tuple is not a tuple at all.  It's just brackets.

x = 42
print x # blastoff

t = (x)

print t # x flows through single-element tuple
