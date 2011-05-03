#_
#__debug__
#__doc__
#__import__
#__name__
#__package__


def abs(o):
    pass #return o.__abs__()

def all():
    pass

def any():
    pass

def apply():
    pass

def bin():
    pass

def bool():
    pass

def buffer():
    pass

def bytearray():
    pass

def bytes():
    pass

def callable():
    pass

def chr():
    pass

def classmethod():
    pass

def cmp(lhs, rhs):
    pass #lhs.__eq__(rhs)

def coerce():
    pass

def compile():
    pass

def complex():
    pass

def copyright():
    pass

def credits():
    pass

def delattr():
    pass

def dir():
    pass

def divmod():
    pass

def enumerate():
    pass

def eval():
    pass

def execfile():
    pass

def exit():
    pass

def file():
    pass

def filter():
    pass

def float():
    pass

def format():
    pass

def frozenset():
    pass

def getattr():
    pass

def globals():
    pass

def hasattr():
    pass

def hash():
    return 42;

def help():
    pass

def hex():
    pass

def id():
    pass

def input():
    pass

def int():
    pass

def intern():
    pass

def isinstance():
    pass

def issubclass():
    pass

def iter():
    pass

def len(iterable):
    pass #return iterable.__len__()

def license():
    pass

def locals():
    pass

def long():
    pass

def map():
    pass

def max():
    pass

def min():
    pass

def next():
    pass

def oct():
    pass

def open():
    pass

def ord():
    pass

def pow():
    pass

#def print():
#    pass

def property():
    pass

def quit():
    pass

def range():
    pass

def raw_input():
    pass

def reduce():
    pass

def reload():
    pass

def repr():
    pass

def reversed():
    pass

def round():
    pass

def setattr():
    pass

def slice():
    pass

def sorted():
    pass

def staticmethod():
    pass

def sum():
    pass

def super():
    pass

def tuple():
    pass

def type():
    pass

def unichr():
    pass

def vars():
    pass

def xrange():
    pass

def zip():
    pass
    
class object:
    
    def __init__(obj, *args, **keywords):
        pass
    
    __class__ = object
    
    def __delattr__(self, name):
        #del self.name
        pass
    
    __dict__ = {}
    __doc__ = "Most base class"
    
    def __format__(self, *args, **keywords):# no in pypy
        pass
    
    def __getattribute__(self, name):
        return self.__dict__[name]
    
    def __hash__(self):
        return hash(self)

    __module__ = "__builtin__"

    def __new__(type, *args, **keywords):
        pass
    
    def __reduce__(self, proto=0):
        pass
    
    def __reduce_ex__(self, proto=0):
        pass

    def __repr__(self):
        return repr(self)

    def __setattr__(self, name, value):
        self.__dict__[name] = value

    def __sizeof__(self): not in pypy
        return 42
    
    def __str__(self):
        return str(self)

    # __subclasshook__ not in pypy
    
    __weakref__ = None

class BaseException(object):
    
    def __getitem__(self, i):
        return self.data[i]

    def __getslice__(self, i, j): # not in pypy
        pass
    
    def __setstate__(self, dict):
        pass
    
    def __unicode__(self, *args, **keywords): #not in pypy
        pass
    
    args = ()
    message = ""

class Exception(BaseException):
    pass

class basestring(object):
    pass
    
class str(basestring):
    def __add__(self, x):
        self.data = self.data + x
    
    def __contains__(self, item):
        return item in self.data
    
    def __eq__(self, other):
        return self.data == other.data
    
    def __ge__(self, other):
        return self.data >= other.data
    
    def __gt__(self, other):
        return self.data > other.data
    
    def __le__(self, other):
        return self.data <= other.data
    
    def __lt__(self, other):
        return self.data < other.data
    
    def __getitem__(self, i):
        return self.data[i]
    
    def __getnewargs__(self, *args, **keywords):
        pass
    
    def __getslice__(self, i, j):
        return self.data[i:j]
    
    def __len__(self):
        return len(self.data)
    
    def __mod__(self, x):
        return self.data % x
    
    def __mul__(self, x):
        return self.data * x
    
    def __ne__(self, other):
        return self.data != other.data
    
    def __rmod__(self, x):
        return x % self.data
    
    def __rmul__(self, x):
        return x * self.data
    
    def capitalize(self):
        pass

    def center(self, width, fillchar=None):
        pass

    def count(self, sub=None, start=None, end=None):
        pass
    
    def decode(self, encoding=None, errors=None):
        pass

    def encode(self, encoding=None, errors=None):
        pass

    def endswith(self, suffix, start=None, end=None):
        pass
    
    def expandtabs(self, tabsize=None):
        pass

    def find(self, sub, start=None, end=None):
        pass

    def format(self, *args, **kwargs):
        pass

    def index(self, sub, start=None, end=None):
        pass

    def isalnum(self):
        pass
    
    def isalpha(self):
        pass

    def isdigit(self):
        pass

    def islower(self):
        pass

    def isspace(self):
        pass

    def istitle(self):
        pass

    def isupper(self):
        pass

    def join(self, iterable):
        pass
    
    def ljust(self, width, fillchar=None):
        pass

    def lower(self):
        pass

    def lstrip(self, chars=None):
        pass

    def partition(self, sep):
        pass

    def replace(self, old, new, count=None):
        pass
    
    def rfind(self, sub, start=None, end=None):
        pass

    def rindex(self, sub, start=None, end=None):
        pass

    def rjust(self, width, fillchar=None):
        pass

    def rpartition(self, sep):
        pass

    def rsplit(self, sep=None, maxsplit=None):
        pass

    def rstrip(self, chars=None):
        pass

    def split(self, sep=None, maxsplit=None):
        pass

    def splitlines(self, keepends=False):
        pass
    
    def startswith(self, prefix, start=None, end=None):
        pass

    def strip(self, chars=None):
        pass

    def swapcase(self):
        pass

    def title(self):
        pass

    def translate(self, table, deletechars=None):
        pass

    def upper(self):
        pass

    def zfill(self, width):
        pass

class unicode(str): # really inherits from basestring and duplicates str methods
    def isnumeric(self):
        pass

    def isdecimal(self):
        pass

class dict(object):
# was class UserDict:
    def __init__(self, dict=None, **kwargs):
        self.data = {}
        if dict is not None:
            self.update(dict)
        if len(kwargs):
            self.update(kwargs)
    def __repr__(self): return repr(self.data)
    def __cmp__(self, dict):
        if isinstance(dict, UserDict):
            return cmp(self.data, dict.data)
        else:
            return cmp(self.data, dict)
    def __len__(self): return len(self.data)
    def __getitem__(self, key):
        if key in self.data:
            return self.data[key]
        if hasattr(self.__class__, "__missing__"):
            return self.__class__.__missing__(self, key)
        raise KeyError(key)
    def __setitem__(self, key, item): self.data[key] = item
    def __delitem__(self, key): del self.data[key]
    def clear(self): self.data.clear()
    def copy(self):
        if self.__class__ is UserDict:
            return UserDict(self.data.copy())
        #import copy
        data = self.data
        try:
            self.data = {}
            c = copy.copy(self)
        finally:
            self.data = data
        c.update(self)
        return c
    def keys(self): return self.data.keys()
    def items(self): return self.data.items()
    def iteritems(self): return self.data.iteritems()
    def iterkeys(self): return self.data.iterkeys()
    def itervalues(self): return self.data.itervalues()
    def values(self): return self.data.values()
    def has_key(self, key): return key in self.data
    def update(self, dict=None, **kwargs):
        if dict is None:
            pass
        elif isinstance(dict, UserDict):
            self.data.update(dict.data)
        elif isinstance(dict, type({})) or not hasattr(dict, 'items'):
            self.data.update(dict)
        else:
            for k, v in dict.items():
                self[k] = v
        if len(kwargs):
            self.data.update(kwargs)
    def get(self, key, failobj=None):
        if key not in self:
            return failobj
        return self[key]
    def setdefault(self, key, failobj=None):
        if key not in self:
            self[key] = failobj
        return self[key]
    def pop(self, key, *args):
        return self.data.pop(key, *args)
    def popitem(self):
        return self.data.popitem()
    def __contains__(self, key):
        return key in self.data
    @classmethod
    def fromkeys(cls, iterable, value=None):
        d = cls()
        for key in iterable:
            d[key] = value
        return d

class list(object):
# was class UserList(collections.MutableSequence):
    def __init__(self, initlist=None):
        self.data = []
        if initlist is not None:
            # XXX should this accept an arbitrary sequence?
            if type(initlist) == type(self.data):
                self.data[:] = initlist
            elif isinstance(initlist, UserList):
                self.data[:] = initlist.data[:]
            else:
                self.data = list(initlist)
    def __repr__(self): return repr(self.data)
    def __lt__(self, other): return self.data <  self.__cast(other)
    def __le__(self, other): return self.data <= self.__cast(other)
    def __eq__(self, other): return self.data == self.__cast(other)
    def __ne__(self, other): return self.data != self.__cast(other)
    def __gt__(self, other): return self.data >  self.__cast(other)
    def __ge__(self, other): return self.data >= self.__cast(other)
    def __cast(self, other):
        if isinstance(other, UserList): return other.data
        else: return other
    def __cmp__(self, other):
        return cmp(self.data, self.__cast(other))
    __hash__ = None # Mutable sequence, so not hashable
    def __contains__(self, item): return item in self.data
    def __len__(self): return len(self.data)
    def __getitem__(self, i): return self.data[i]
    def __setitem__(self, i, item): self.data[i] = item
    def __delitem__(self, i): del self.data[i]
    def __getslice__(self, i, j):
        i = max(i, 0); j = max(j, 0)
        return self.__class__(self.data[i:j])
    def __setslice__(self, i, j, other):
        i = max(i, 0); j = max(j, 0)
        if isinstance(other, UserList):
            self.data[i:j] = other.data
        elif isinstance(other, type(self.data)):
            self.data[i:j] = other
        else:
            self.data[i:j] = list(other)
    def __delslice__(self, i, j):
        i = max(i, 0); j = max(j, 0)
        del self.data[i:j]
    def __add__(self, other):
        if isinstance(other, UserList):
            return self.__class__(self.data + other.data)
        elif isinstance(other, type(self.data)):
            return self.__class__(self.data + other)
        else:
            return self.__class__(self.data + list(other))
    def __radd__(self, other):
        if isinstance(other, UserList):
            return self.__class__(other.data + self.data)
        elif isinstance(other, type(self.data)):
            return self.__class__(other + self.data)
        else:
            return self.__class__(list(other) + self.data)
    def __iadd__(self, other):
        if isinstance(other, UserList):
            self.data += other.data
        elif isinstance(other, type(self.data)):
            self.data += other
        else:
            self.data += list(other)
        return self
    def __mul__(self, n):
        return self.__class__(self.data*n)
    __rmul__ = __mul__
    def __imul__(self, n):
        self.data *= n
        return self
    def append(self, item): self.data.append(item)
    def insert(self, i, item): self.data.insert(i, item)
    def pop(self, i=-1): return self.data.pop(i)
    def remove(self, item): self.data.remove(item)
    def count(self, item): return self.data.count(item)
    def index(self, item, *args): return self.data.index(item, *args)
    def reverse(self): self.data.reverse()
    def sort(self, *args, **kwds): self.data.sort(*args, **kwds)
    def extend(self, other):
        if isinstance(other, UserList):
            self.data.extend(other.data)
        else:
            self.data.extend(other)

# From pypy
class _BaseSet(object):
#was class BaseSet(object):
    """Common base class for mutable and immutable sets."""

    __slots__ = ['_data']

    # Constructor

    def __init__(self):
        """This is an abstract class."""
        # Don't call this from a concrete subclass!
        if self.__class__ is BaseSet:
            raise TypeError, ("BaseSet is an abstract class.  "
                              "Use Set or ImmutableSet.")

    # Standard protocols: __len__, __repr__, __str__, __iter__

    def __len__(self):
        """Return the number of elements of a set."""
        return len(self._data)

    def __repr__(self):
        """Return string representation of a set.

        This looks like 'Set([<list of elements>])'.
        """
        return self._repr()

    # __str__ is the same as __repr__
    __str__ = __repr__

    def _repr(self, sorted=False):
        elements = self._data.keys()
        if sorted:
            elements.sort()
        return '%s(%r)' % (self.__class__.__name__, elements)

    def __iter__(self):
        """Return an iterator over the elements or a set.

        This is the keys iterator for the underlying dict.
        """
        return self._data.iterkeys()

    # Three-way comparison is not supported.  However, because __eq__ is
    # tried before __cmp__, if Set x == Set y, x.__eq__(y) returns True and
    # then cmp(x, y) returns 0 (Python doesn't actually call __cmp__ in this
    # case).

    def __cmp__(self, other):
        raise TypeError, "can't compare sets using cmp()"

    # Equality comparisons using the underlying dicts.  Mixed-type comparisons
    # are allowed here, where Set == z for non-Set z always returns False,
    # and Set != z always True.  This allows expressions like "x in y" to
    # give the expected result when y is a sequence of mixed types, not
    # raising a pointless TypeError just because y contains a Set, or x is
    # a Set and y contain's a non-set ("in" invokes only __eq__).
    # Subtle:  it would be nicer if __eq__ and __ne__ could return
    # NotImplemented instead of True or False.  Then the other comparand
    # would get a chance to determine the result, and if the other comparand
    # also returned NotImplemented then it would fall back to object address
    # comparison (which would always return False for __eq__ and always
    # True for __ne__).  However, that doesn't work, because this type
    # *also* implements __cmp__:  if, e.g., __eq__ returns NotImplemented,
    # Python tries __cmp__ next, and the __cmp__ here then raises TypeError.

    def __eq__(self, other):
        if isinstance(other, BaseSet):
            return self._data == other._data
        else:
            return False

    def __ne__(self, other):
        if isinstance(other, BaseSet):
            return self._data != other._data
        else:
            return True

    # Copying operations

    def copy(self):
        """Return a shallow copy of a set."""
        result = self.__class__()
        result._data.update(self._data)
        return result

    __copy__ = copy # For the copy module

    def __deepcopy__(self, memo):
        """Return a deep copy of a set; used by copy module."""
        # This pre-creates the result and inserts it in the memo
        # early, in case the deep copy recurses into another reference
        # to this same set.  A set can't be an element of itself, but
        # it can certainly contain an object that has a reference to
        # itself.
        #from copy import deepcopy
        result = self.__class__()
        memo[id(self)] = result
        data = result._data
        value = True
        for elt in self:
            data[deepcopy(elt, memo)] = value
        return result

    # Standard set operations: union, intersection, both differences.
    # Each has an operator version (e.g. __or__, invoked with |) and a
    # method version (e.g. union).
    # Subtle:  Each pair requires distinct code so that the outcome is
    # correct when the type of other isn't suitable.  For example, if
    # we did "union = __or__" instead, then Set().union(3) would return
    # NotImplemented instead of raising TypeError (albeit that *why* it
    # raises TypeError as-is is also a bit subtle).

    def __or__(self, other):
        """Return the union of two sets as a new set.

        (I.e. all elements that are in either set.)
        """
        if not isinstance(other, BaseSet):
            return NotImplemented
        return self.union(other)

    def union(self, other):
        """Return the union of two sets as a new set.

        (I.e. all elements that are in either set.)
        """
        result = self.__class__(self)
        result._update(other)
        return result

    def __and__(self, other):
        """Return the intersection of two sets as a new set.

        (I.e. all elements that are in both sets.)
        """
        if not isinstance(other, BaseSet):
            return NotImplemented
        return self.intersection(other)

    def intersection(self, other):
        """Return the intersection of two sets as a new set.

        (I.e. all elements that are in both sets.)
        """
        if not isinstance(other, BaseSet):
            other = Set(other)
        if len(self) <= len(other):
            little, big = self, other
        else:
            little, big = other, self
        common = ifilter(big._data.has_key, little)
        return self.__class__(common)

    def __xor__(self, other):
        """Return the symmetric difference of two sets as a new set.

        (I.e. all elements that are in exactly one of the sets.)
        """
        if not isinstance(other, BaseSet):
            return NotImplemented
        return self.symmetric_difference(other)

    def symmetric_difference(self, other):
        """Return the symmetric difference of two sets as a new set.

        (I.e. all elements that are in exactly one of the sets.)
        """
        result = self.__class__()
        data = result._data
        value = True
        selfdata = self._data
        try:
            otherdata = other._data
        except AttributeError:
            otherdata = Set(other)._data
        for elt in ifilterfalse(otherdata.has_key, selfdata):
            data[elt] = value
        for elt in ifilterfalse(selfdata.has_key, otherdata):
            data[elt] = value
        return result

    def  __sub__(self, other):
        """Return the difference of two sets as a new Set.

        (I.e. all elements that are in this set and not in the other.)
        """
        if not isinstance(other, BaseSet):
            return NotImplemented
        return self.difference(other)

    def difference(self, other):
        """Return the difference of two sets as a new Set.

        (I.e. all elements that are in this set and not in the other.)
        """
        result = self.__class__()
        data = result._data
        try:
            otherdata = other._data
        except AttributeError:
            otherdata = Set(other)._data
        value = True
        for elt in ifilterfalse(otherdata.has_key, self):
            data[elt] = value
        return result

    # Membership test

    def __contains__(self, element):
        """Report whether an element is a member of a set.

        (Called in response to the expression `element in self'.)
        """
        try:
            return element in self._data
        except TypeError:
            transform = getattr(element, "__as_temporarily_immutable__", None)
            if transform is None:
                raise # re-raise the TypeError exception we caught
            return transform() in self._data

    # Subset and superset test

    def issubset(self, other):
        """Report whether another set contains this set."""
        self._binary_sanity_check(other)
        if len(self) > len(other):  # Fast check for obvious cases
            return False
        for elt in ifilterfalse(other._data.has_key, self):
            return False
        return True

    def issuperset(self, other):
        """Report whether this set contains another set."""
        self._binary_sanity_check(other)
        if len(self) < len(other):  # Fast check for obvious cases
            return False
        for elt in ifilterfalse(self._data.has_key, other):
            return False
        return True

    # Inequality comparisons using the is-subset relation.
    __le__ = issubset
    __ge__ = issuperset

    def __lt__(self, other):
        self._binary_sanity_check(other)
        return len(self) < len(other) and self.issubset(other)

    def __gt__(self, other):
        self._binary_sanity_check(other)
        return len(self) > len(other) and self.issuperset(other)

    # Assorted helpers

    def _binary_sanity_check(self, other):
        # Check that the other argument to a binary operation is also
        # a set, raising a TypeError otherwise.
        if not isinstance(other, BaseSet):
            raise TypeError, "Binary operation only permitted between sets"

    def _compute_hash(self):
        # Calculate hash code for a set by xor'ing the hash codes of
        # the elements.  This ensures that the hash code does not depend
        # on the order in which elements are added to the set.  This is
        # not called __hash__ because a BaseSet should not be hashable;
        # only an ImmutableSet is hashable.
        result = 0
        for elt in self:
            result ^= hash(elt)
        return result

    def _update(self, iterable):
        # The main loop for update() and the subclass __init__() methods.
        data = self._data

        # Use the fast update() method when a dictionary is available.
        if isinstance(iterable, BaseSet):
            data.update(iterable._data)
            return

        value = True

        if type(iterable) in (list, tuple, xrange):
            # Optimized: we know that __iter__() and next() can't
            # raise TypeError, so we can move 'try:' out of the loop.
            it = iter(iterable)
            while True:
                try:
                    for element in it:
                        data[element] = value
                    return
                except TypeError:
                    transform = getattr(element, "__as_immutable__", None)
                    if transform is None:
                        raise # re-raise the TypeError exception we caught
                    data[transform()] = value
        else:
            # Safe: only catch TypeError where intended
            for element in iterable:
                try:
                    data[element] = value
                except TypeError:
                    transform = getattr(element, "__as_immutable__", None)
                    if transform is None:
                        raise # re-raise the TypeError exception we caught
                    data[transform()] = value

class set(_BaseSet):
# was class Set(BaseSet):
    """ Mutable set class."""

    __slots__ = []

    # BaseSet + operations requiring mutability; no hashing

    def __init__(self, iterable=None):
        """Construct a set from an optional iterable."""
        self._data = {}
        if iterable is not None:
            self._update(iterable)

    def __getstate__(self):
        # getstate's results are ignored if it is not
        return self._data,

    def __setstate__(self, data):
        self._data, = data

    def __hash__(self):
        """A Set cannot be hashed."""
        # We inherit object.__hash__, so we must deny this explicitly
        raise TypeError, "Can't hash a Set, only an ImmutableSet."

    # In-place union, intersection, differences.
    # Subtle:  The xyz_update() functions deliberately return None,
    # as do all mutating operations on built-in container types.
    # The __xyz__ spellings have to return self, though.

    def __ior__(self, other):
        """Update a set with the union of itself and another."""
        self._binary_sanity_check(other)
        self._data.update(other._data)
        return self

    def union_update(self, other):
        """Update a set with the union of itself and another."""
        self._update(other)

    def __iand__(self, other):
        """Update a set with the intersection of itself and another."""
        self._binary_sanity_check(other)
        self._data = (self & other)._data
        return self

    def intersection_update(self, other):
        """Update a set with the intersection of itself and another."""
        if isinstance(other, BaseSet):
            self &= other
        else:
            self._data = (self.intersection(other))._data

    def __ixor__(self, other):
        """Update a set with the symmetric difference of itself and another."""
        self._binary_sanity_check(other)
        self.symmetric_difference_update(other)
        return self

    def symmetric_difference_update(self, other):
        """Update a set with the symmetric difference of itself and another."""
        data = self._data
        value = True
        if not isinstance(other, BaseSet):
            other = Set(other)
        if self is other:
            self.clear()
        for elt in other:
            if elt in data:
                del data[elt]
            else:
                data[elt] = value

    def __isub__(self, other):
        """Remove all elements of another set from this set."""
        self._binary_sanity_check(other)
        self.difference_update(other)
        return self

    def difference_update(self, other):
        """Remove all elements of another set from this set."""
        data = self._data
        if not isinstance(other, BaseSet):
            other = Set(other)
        if self is other:
            self.clear()
        for elt in ifilter(data.has_key, other):
            del data[elt]

    # Python dict-like mass mutations: update, clear

    def update(self, iterable):
        """Add all values from an iterable (such as a list or file)."""
        self._update(iterable)

    def clear(self):
        """Remove all elements from this set."""
        self._data.clear()

    # Single-element mutations: add, remove, discard

    def add(self, element):
        """Add an element to a set.

        This has no effect if the element is already present.
        """
        try:
            self._data[element] = True
        except TypeError:
            transform = getattr(element, "__as_immutable__", None)
            if transform is None:
                raise # re-raise the TypeError exception we caught
            self._data[transform()] = True

    def remove(self, element):
        """Remove an element from a set; it must be a member.

        If the element is not a member, raise a KeyError.
        """
        try:
            del self._data[element]
        except TypeError:
            transform = getattr(element, "__as_temporarily_immutable__", None)
            if transform is None:
                raise # re-raise the TypeError exception we caught
            del self._data[transform()]

    def discard(self, element):
        """Remove an element from a set if it is a member.

        If the element is not a member, do nothing.
        """
        try:
            self.remove(element)
        except KeyError:
            pass

    def pop(self):
        """Remove and return an arbitrary set element."""
        return self._data.popitem()[0]

    def __as_immutable__(self):
        # Return a copy of self as an immutable set
        return ImmutableSet(self)

    def __as_temporarily_immutable__(self):
        # Return self wrapped in a temporarily immutable set
        return _TemporarilyImmutableSet(self)

