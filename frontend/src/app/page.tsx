import Header from "@/components/Header";
import LeftSidebar from "@/components/LeftSidebar";
import RightSidebar from "@/components/RightSidebar";
import MainContent from "@/components/MainContent";
import InitReport from "@/components/InitReport";

export default function Home() {
  return (
    <>
      <InitReport />
      <Header />
      <div className="flex flex-1 overflow-hidden">
        <LeftSidebar />
        <MainContent />
        <RightSidebar />
      </div>
    </>
  );
}
