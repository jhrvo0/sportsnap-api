import dynamic from 'next/dynamic';

export const DynamicMap = dynamic(() => import('./MapPicker'), {
  ssr: false,
  loading: () => (
    <div className="w-full bg-ink-50 rounded-[1.5rem] flex items-center justify-center border border-ink-100 animate-pulse" style={{ height: '300px' }}>
      <p className="text-ink-400 text-sm font-medium">Carregando Mapa...</p>
    </div>
  )
});
