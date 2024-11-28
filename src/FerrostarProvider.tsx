import { createContext, useState } from "react";

import { ExpoFerrostarModule } from "./ExpoFerrostar.types";

type FerrostarContext = {
  refs: {
    [id: string]: ExpoFerrostarModule | undefined;
  };
};

export const FerrostarContext = createContext<FerrostarContext>({
  refs: {},
});

type FerrostarProviderProps = {
  children: React.ReactNode;
};

export const FerrostarProvider = ({ children }: FerrostarProviderProps) => {
  const [refs] = useState({});

  return (
    <FerrostarContext.Provider value={{ refs }}>
      {children}
    </FerrostarContext.Provider>
  );
};
