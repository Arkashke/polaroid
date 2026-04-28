package polaroid.client.utils.performance;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Пул для Vector3d объектов - часто используются в модулях элитры
 */
public class Vector3dPool {
    
    private static final ObjectPool<MutableVector3d> POOL = 
        new ObjectPool<>(MutableVector3d::new, 100);
    
    public static MutableVector3d acquire() {
        return POOL.acquire();
    }
    
    public static MutableVector3d acquire(double x, double y, double z) {
        MutableVector3d vec = POOL.acquire();
        vec.set(x, y, z);
        return vec;
    }
    
    public static void release(MutableVector3d vec) {
        POOL.release(vec);
    }
    
    /**
     * Изменяемая версия Vector3d для переиспользования
     */
    public static class MutableVector3d {
        public double x, y, z;
        
        public MutableVector3d() {
            this(0, 0, 0);
        }
        
        public MutableVector3d(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public void set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public void set(Vector3d vec) {
            this.x = vec.x;
            this.y = vec.y;
            this.z = vec.z;
        }
        
        public Vector3d toImmutable() {
            return new Vector3d(x, y, z);
        }
        
        public double distanceTo(MutableVector3d other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double dz = this.z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        
        public double distanceTo(Vector3d other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double dz = this.z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }
}


