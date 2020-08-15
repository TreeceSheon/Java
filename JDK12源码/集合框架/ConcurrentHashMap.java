## 添加元素
final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        //  散布矩阵（高第十六位互与）计算哈希值
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh; K fk; V fv;
            //  创建数组逻辑，并发状况下只允许一个线程获得创建资格，其它线程直接yeild让出CPU及自旋
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            //  数组不为空，但该key对应的slot槽位为空，采用CAS操作直接设置，成功则返回，失败返回while继续自旋
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value)))
                    break;                   // no lock when adding to empty bin
            }
            //  如果当前数组正在被其它线程扩容，并且该数组上此key值对应的slot已经扩容完成，则帮助其扩容其它的slot到新数组，并返回扩容后的数组，自旋设置值到新数组中。
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            //  当设置了非空值不覆盖的逻辑时进入
            else if (onlyIfAbsent // check first node without acquiring lock
                     && fh == hash
                     && ((fk = f.key) == key || (fk != null && key.equals(fk)))
                     && (fv = f.val) != null)
                return fv;
             //  正片，当前数组、槽位都不为空，也未扩容
            else {
                V oldVal = null;
                //  获取当前对应槽位的第一个node的锁资源
                synchronized (f) {
                    //  必须判断当前槽位首部是否为最初的结点，有可能在获取锁时该节点正在被扩容但未完成，导致该线程被阻塞，直到刚刚被扩容完成，首位已经变成了forwardNode,哈希值为-1
                    if (tabAt(tab, i) == f) {
                        //  是否为链表？ 链表的头节点哈希值为正则为链表，执行链表插入逻辑
                        if (fh >= 0) {
                            binCount = 1;
                            //  遍历插值，binCount记录当前链表长度，可能会遍历到重复key值覆盖，然后直接退出，所以不一定准确
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K,V> pred = e;
                                //  尾插法
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key, value);
                                    break;
                                }
                            }
                        }
                        // 为红黑树头节点，执行红黑树插入操作
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            binCount = 2;
                            //  红黑树插入逻辑
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                        //  占位符，用于当前结点正在计算值的情况，如computeIfAbsent
                        else if (f instanceof ReservationNode)
                            throw new IllegalStateException("Recursive update");
                    }
                }
                //  检查是否需要树化
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        //  树化
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        //  增加size长度
        addCount(1L, binCount);
        return null;
    }
    
## 增加size，addCount()
        /**
            x，一般都为1，即某一个线程put元素成功后修改map长度的变量值
            check，即bitCount，用于检查是否需要扩容
        */
       private final void addCount(long x, int check) {
        /*  CounterCell,一种用于存储当前cell中数值x的类，x是volatile的长整型数值，它最后用于加和计算总长度；
            b, baseCount的临时变量，baseCount是当无多线程竞争时，存储map长度的属性；
            s
        */
        CounterCell[] cs; long b, s;
        //  counterCells，类的成员变量，用于当出现多线程竞争增加map长度时，临时存储长度的增加值
        //  这里的逻辑是，如果①当前的countCells已经初始化，或者②尚未初始化同时CAS增加bitCount失败，则进入
        if ((cs = counterCells) != null ||
            !U.compareAndSetLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            CounterCell c; long v; int m;
            boolean uncontended = true;
            /*  当前countCells未初始化，或者初始化但长度为0，又或者虽然已被初始化，但该修改map长度的线程在当前countCells中对应的槽位没有被初始化的情况，以及
             *  虽然以上三个条件都满足，但是对该线程对应的槽位进行CAS修改失败的四种情况，四种情况按照先后顺序判断；
            */
            if (cs == null || (m = cs.length - 1) < 0 ||
                (c = cs[ThreadLocalRandom.getProbe() & m]) == null ||
                !(uncontended =
                  U.compareAndSetLong(c, CELLVALUE, v = c.value, v + x))) {
                //  说明当前多线程竞争相当激烈，或者需要做一些初始化的操作，即为fullAddCount实现的功能。
                //  更多fullAddCount的说明，请查看fullAddCount方法，157行。
                fullAddCount(x, uncontended);
                return;
            }
            // check 小于等于1，说明不需要扩容
            if (check <= 1)
                return;
            // 将baseCount和counterCells的所有数字加和，求得当前长度。
            s = sumCount();
        }
        //  以下是检查是否需要扩容的逻辑
        if (check >= 0) {
            Node<K,V>[] tab, nt; int n, sc;
            /*
             *  sizeCtl:默认为0,用来控制table的初始化和扩容操作.它的数值有以下含义
             *  -1 :代表table正在初始化,
             *  -N: 表示正有N-1个线程执行扩容操作
             *  >0: 如果table已经初始化,代表table容量,默认为table大小的0.75,如果还未初始化,代表需要初始化的大小
            */
            
            //  自旋操作，若当前长度大于扩容阈值，并且当前的table不为空（由于当前未给table加锁，所以可能存在table被其它线程修改的情况），且长度小于最大允许值
            while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                   (n = tab.length) < MAXIMUM_CAPACITY) {
                //  一个特殊常量，用于标记之后情况中，正在扩容，或者正在帮助扩容的线程数。
                int rs = resizeStamp(n) << RESIZE_STAMP_SHIFT;
                // sc = -1表示数组正在初始化，-N表示当前有N-1个线程正在扩容。显然我们这里可以排除初始化
                if (sc < 0) {
                    //  MAX_RESIZERS，用于标记最大允许帮助扩容的线程数量；当帮助扩容线程数已经超过最大允许数量时，或者sc == rs + 1（不懂啥意思）
                    //  或者(nt = nextTable)，即表示扩容即将完成，已经将table指向了新生成的数组，或者（transferIndex <= 0），即说明当前table所有的slot都已经处于扩容期，不需要多的帮助
                    if (sc == rs + MAX_RESIZERS || sc == rs + 1 ||
                        (nt = nextTable) == null || transferIndex <= 0)
                        break;
                    //  能走到这，说明需要帮助扩容，则尝试帮助，失败则自旋
                    if (U.compareAndSetInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }
                // 说明需要扩容，但是目前尚无任何线程扩容数组，则当前线程为首个进行扩容的线程
                else if (U.compareAndSetInt(this, SIZECTL, sc, rs + 2))
                    transfer(tab, null);
                s = sumCount();
            }
        }
    }

## fullAddCount
/**
*  用于初始化CountCells数组（逻辑1），初始化该数组对应的某一个slot（逻辑2），或者多线程竞争下的CAS优化（逻辑3）。
*  在超多线程并发的情况下，CAS乐观锁已不适合该场景，但同时为了使用重量级悲观锁，fullAddCount采取了LongAdder的设计理念，将所有自旋的CAS增量都放到一个数组中，
*  每个线程计算一个随机值，按照哈希的原理均匀散射到该数组中，即countCells，最后计算长度的时候遍历countCells，并加上baseCount，得到map的真正长度。
*/
private final void fullAddCount(long x, boolean wasUncontended) {
        int h;
        if ((h = ThreadLocalRandom.getProbe()) == 0) {
            ThreadLocalRandom.localInit();      // force initialization
            h = ThreadLocalRandom.getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // True if last slot nonempty
        for (;;) {
            CounterCell[] cs; CounterCell c; int n; long v;
            // 逻辑2和3
            if ((cs = counterCells) != null && (n = cs.length) > 0) {
                //  逻辑2，初始化某一个对应的slot
                if ((c = cs[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {            // Try to attach new Cell
                        CounterCell r = new CounterCell(x); // Optimistic create
                        //  如果此时countCells未被使用，则CAS标记并使用。注意，countCells为volatile修饰值，对其原子型赋值具有锁的功能，全局可见
                        if (cellsBusy == 0 &&
                            U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
                            boolean created = false;
                            try {               // Recheck under lock
                                CounterCell[] rs; int m, j;
                                // 再次检查该slot是否为空
                                if ((rs = counterCells) != null &&
                                    (m = rs.length) > 0 &&
                                    rs[j = (m - 1) & h] == null) {
                                    //  乐观修改
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                // 释放该简单的锁
                                cellsBusy = 0;
                            }
                            //  成功添加，任务完成退出
                            if (created)
                                break;
                            //  添加失败，说明当前slot在cellsBusy获取时就正在被修改，自旋，进入逻辑3
                            continue;           // Slot is now non-empty
                        }
                    }
                    // 因为cellsbusy == 1，第一次冲突，然后自旋。当两次冲突后会修改collide为true，导致扩容CountCells
                    collide = false;
                }
                /*  如果CAS冲突，则传入的wasUncontended就是true，无需修改，继续往下。当传入的wasUncontended本是false，并执行到这的时候，
                 *  说明在该函数内部发生了冲突然后进入条件体并自旋。
                */
                else if (!wasUncontended)       // CAS already known to fail
                    wasUncontended = true;      // Continue after rehash
                //  再次尝试cas修改该线程对应的cell，若失败，继续
                else if (U.compareAndSetLong(c, CELLVALUE, v = c.value, v + x))
                    break;
                //  如果countCells数组已经不能再扩容了，那么忽略掉冲突标志
                else if (counterCells != cs || n >= NCPU)
                    collide = false;            // At max size or stale
                //  能执行到这，说明前面已经冲突了，那么将冲突位置1，并且自旋，当改线程第二次到达这里的时候，说明又冲突了。冲突发生两次，说明形式严峻，必须扩容countCells.
                else if (!collide)
                    collide = true;
                //  修改cellsBusy标志，准备扩容countCells
                else if (cellsBusy == 0 &&
                         U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
                    try {
                        if (counterCells == cs) // Expand table unless stale
                            counterCells = Arrays.copyOf(cs, n << 1);
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;                   // Retry with expanded table
                }
                h = ThreadLocalRandom.advanceProbe(h);
            }
            //  逻辑1，CAS竞争初始化CountCells数组，该if和该方法的第一个if条件句并列。
            else if (cellsBusy == 0 && counterCells == cs &&
                     U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
                boolean init = false;
                try {                           // Initialize table
                    if (counterCells == cs) {
                        CounterCell[] rs = new CounterCell[2];
                        rs[h & 1] = new CounterCell(x);
                        counterCells = rs;
                        init = true;
                    }
                } finally {
                    cellsBusy = 0;
                }
                if (init)
                    break;
            }
            //  逻辑1衍生，在countCells未初始化时，多个线程竞争地想初始化countCells，并且该线程失败，则试着在baseCount上加1，如果成功也算是目标达成，退出。
            else if (U.compareAndSetLong(this, BASECOUNT, v = baseCount, v + x))
                break;                          // Fall back on using base
        }
    }
    
## 扩容，change()
    /**
     *  扩容table，这里用到了一个newTable。中心思想是根据计算机的CPU核数算出一个步长，并从右往左设定一个扩容起点i，每个线程赋负责[i-stride,i]的长度的slot扩容。
     *  当线程扩容完一个stride后，会根据具体情况判断是否需要advance（即继续前进扩容），当该线程扩容完毕，修改finish=true，完成扩容退出逻辑。
     */
    private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        int n = tab.length, stride;
        //  计算一个扩容步长
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            stride = MIN_TRANSFER_STRIDE; // subdivide range
        //  初始情况，该线程为第一个扩容的线程。初始化nextTable，用于临时存储一个新的数组，长度为旧数组的两倍。
        if (nextTab == null) {            // initiating
            try {
                @SuppressWarnings("unchecked")
                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
                nextTab = nt;
            } catch (Throwable ex) {      // try to cope with OOME
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            // 初始情况设定扩容起点为旧数组的最后一位
            transferIndex = n;
        }
        int nextn = nextTab.length;
        ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
        boolean advance = true;
        boolean finishing = false; // to ensure sweep before committing nextTab
        for (int i = 0, bound = 0;;) {
            Node<K,V> f; int fh;
            while (advance) {
                int nextIndex, nextBound;
                if (--i >= bound || finishing)
                    advance = false;
                else if ((nextIndex = transferIndex) <= 0) {
                    i = -1;
                    advance = false;
                }
                else if (U.compareAndSetInt
                         (this, TRANSFERINDEX, nextIndex,
                          nextBound = (nextIndex > stride ?
                                       nextIndex - stride : 0))) {
                    // 根据stride和transferIndex起点计算当前扩容边界，如果stride为2，transferIndex为4，则扩容边界为4-2=2
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            //  说明扩容完成，准备各种退出逻辑
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                //  收尾工作完成，做最后的退出工作
                if (finishing) {
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
                //  完成任务，将sc数值减一，表明该线程完成任务准备退出，将当前工作于扩容的线程数量减1
                if (U.compareAndSetInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    /*
                    * 第一个扩容的线程，执行transfer方法之前，会设置 sizeCtl = (resizeStamp(n) << RESIZE_STAMP_SHIFT) + 2)
                    * 后续帮其扩容的线程，执行transfer方法之前，会设置 sizeCtl = sizeCtl+1
                    * 每一个退出transfer的方法的线程，退出之前，会设置 sizeCtl = sizeCtl-1
                    * 那么最后一个线程退出时：
                    * 必然有sc == (resizeStamp(n) << RESIZE_STAMP_SHIFT) + 2)，即 (sc - 2) == resizeStamp(n) << RESIZE_STAMP_SHIFT
                    */
                    //  退出即可，因为不是当前剩下的最后一个线程
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                        return;
                    // 说明是最后一个退出的线程，需要将finish置为1
                    finishing = advance = true;
                    //  检查是否迁移完毕
                    i = n; // recheck before commit
                }
            }
            /* 该slot的所有数据迁移完成，在该slot的首位插入forwardNode，标明该位置的所有数据已经暂时存储在newTable中，
             * 方便其它线程读操作，同时也表明该数组正在扩容，需要其它线程帮助。
            */ 
            else if ((f = tabAt(tab, i)) == null)
                // 修改成功，继续transfer其它slot
                advance = casTabAt(tab, i, null, fwd);
            //  如果当前的slot的hash值为MOVED,说明其它线程已经完成了该slot的迁移工作，继续向前
            else if ((fh = f.hash) == MOVED)
                advance = true; // already processed
            else {
                //  正片，数据迁移
                synchronized (f) {
                    //  链表型的扩容迁移
                    if (tabAt(tab, i) == f) {
                        Node<K,V> ln, hn;
                        if (fh >= 0) {
                            int runBit = fh & n;
                            Node<K,V> lastRun = f;
                            for (Node<K,V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            }
                            else {
                                hn = lastRun;
                                ln = null;
                            }
                            //  根据链表中的hash值高位不同，分为两组，分别插入到新数组中不同的slot
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                if ((ph & n) == 0)
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                        //  红黑树迁移逻辑
                        else if (f instanceof TreeBin) {
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> lo = null, loTail = null;
                            TreeNode<K,V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
                            for (Node<K,V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K,V> p = new TreeNode<K,V>
                                    (h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null)
                                        lo = p;
                                    else
                                        loTail.next = p;
                                    loTail = p;
                                    ++lc;
                                }
                                else {
                                    if ((p.prev = hiTail) == null)
                                        hi = p;
                                    else
                                        hiTail.next = p;
                                    hiTail = p;
                                    ++hc;
                                }
                            }
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                (hc != 0) ? new TreeBin<K,V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                (lc != 0) ? new TreeBin<K,V>(hi) : t;
                            //  根据红黑树中的hash值高位不同，分为两组，分别插入到新数组中不同的slot
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                        else if (f instanceof ReservationNode)
                            throw new IllegalStateException("Recursive update");
                    }
                }
            }
        }
    }
